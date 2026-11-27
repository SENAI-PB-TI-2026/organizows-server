"""Diagnostico da convencao e do historico Flyway, sem acessar bancos."""
import collections
import os
from pathlib import Path
import re
import subprocess

ROOT = "src/main/resources/db/migration"
VERSIONED = re.compile(r"V([0-9]+(?:[._][0-9]+)*)__([a-zA-Z0-9][a-zA-Z0-9_-]*)\.sql")
REPEATABLE = re.compile(r"R__[a-zA-Z0-9][a-zA-Z0-9_-]*\.sql")

def git(*args):
    return subprocess.check_output(["git", *args]).decode("utf-8", errors="surrogateescape")

def sql_files():
    return sorted(p for p in git("ls-files", "-z", "--", ROOT).split("\0")
                  if p.lower().endswith(".sql"))

def escape(value, property_value=False):
    value = str(value).replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
    if property_value:
        value = value.replace(":", "%3A").replace(",", "%2C")
    return value

def warning(path, message, strict):
    level = "error" if strict else "warning"
    print(f"::{level} file={escape(path, True)}::{escape(message)}")

def version_key(raw):
    parts = [int(part) for part in re.split(r"[._]", raw)]
    while len(parts) > 1 and parts[-1] == 0:
        parts.pop()
    return tuple(parts)

def inspect_history(base, files):
    findings = []
    versions = collections.defaultdict(list)
    for path in files:
        name = Path(path).name
        match = VERSIONED.fullmatch(name)
        if match:
            versions[version_key(match.group(1))].append(path)
        elif not REPEATABLE.fullmatch(name):
            findings.append((path, "Nome fora da convencao V<versao>__descricao.sql ou R__descricao.sql."))
        if Path(path).is_symlink() or not Path(path).is_file():
            findings.append((path, "Migration nao e um arquivo regular."))
    for version, paths in versions.items():
        if len(paths) > 1:
            for path in paths:
                findings.append((path, f"Versao Flyway duplicada: {'.'.join(map(str, version))}."))
    if base:
        if not re.fullmatch(r"[0-9a-f]{40}", base):
            raise ValueError("SHA base invalido")
        # A ausencia de deteccao de renames representa rename como delete + add.
        diff = git("diff", "--no-renames", "--name-status", "-z", base, "HEAD", "--", ROOT)
        fields = diff.rstrip("\0").split("\0") if diff else []
        base_files = git("ls-tree", "-r", "-z", "--name-only", base, "--", ROOT).split("\0")
        base_versions = [version_key(match.group(1)) for path in base_files
                         if (match := VERSIONED.fullmatch(Path(path).name))]
        highest_base = max(base_versions, default=None)
        for index in range(0, len(fields), 2):
            status, path = fields[index:index + 2]
            if (status.startswith(("M", "D", "T")) and path.lower().endswith(".sql")
                    and not REPEATABLE.fullmatch(Path(path).name)):
                findings.append((path, "Migration existente modificada, removida ou renomeada em relacao a base do PR. Revise o historico antes de aplicar."))
            match = VERSIONED.fullmatch(Path(path).name)
            if status == "A" and match and highest_base is not None and version_key(match.group(1)) <= highest_base:
                findings.append((path, "Nova migration com versao anterior ou igual a maior versao na base do PR. Revise a ordem de aplicacao."))
    return findings

if __name__ == "__main__":
    files = sql_files()
    strict = os.getenv("MIGRATION_STRICT", "false").lower() == "true"
    base = os.getenv("BASE_SHA", "")
    findings = inspect_history(base, files)
    print(f"Arquivos SQL analisados: {len(files)}. Apontamentos: {len(findings)}.")
    if not files:
        print("::warning::Nenhuma migration SQL versionada no diretorio esperado.")
    if not base:
        print("Comparacao de historico indisponivel fora de pull_request.")
    for path, message in findings:
        warning(path, message, strict)
    summary = os.getenv("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a", encoding="utf-8") as handle:
            handle.write(f"## Integridade das migrations\n\n"
                         f"- Arquivos analisados: {len(files)}\n"
                         f"- Apontamentos: {len(findings)}\n"
                         f"- Modo estrito: {'sim' if strict else 'nao'}\n"
                         f"- Comparacao com a base do PR: {'sim' if base else 'nao'}\n")
    raise SystemExit(1 if strict and findings else 0)
