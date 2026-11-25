package br.senai.organizows.config;

import java.sql.SQLException;

import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.boot.diagnostics.FailureAnalyzer;
import org.springframework.core.env.Environment;

public class DevConnectionFailureAnalyzer implements FailureAnalyzer {

    private final Environment environment;

    public DevConnectionFailureAnalyzer(Environment environment) {
        this.environment = environment;
    }

    @Override
    public FailureAnalysis analyze(Throwable failure) {
        if (!environment.matchesProfiles("dev")) {
            return null;
        }

        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sqlException && sqlException.getSQLState() != null) {
                String sqlState = sqlException.getSQLState();
                if (sqlState.startsWith("28")) {
                    return new FailureAnalysis(
                            "O banco de desenvolvimento recusou a autenticação.",
                            "Confira DEV_DB_USER e DEV_DB_PASSWORD no ambiente e no .env da raiz.",
                            cause
                    );
                }
                if (sqlState.startsWith("08") || sqlState.equals("90067")) {
                    return new FailureAnalysis(
                            "Falha ao estabelecer conexão com o banco de desenvolvimento.",
                            "Confira se o banco está ativo e se host, porta e banco em DEV_DB_URL estão corretos.",
                            cause
                    );
                }
            }
        }
        return null;
    }
}
