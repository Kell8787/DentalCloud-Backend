package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Makes legacy appointment columns optional without assuming one exact old schema. */
public class V13__allow_legacy_appointment_columns_null extends BaseJavaMigration {
    private static final List<String> LEGACY_COLUMNS = List.of(
            "appointment_date", "hora_desde", "hora_hasta", "patient_email", "patient_name",
            "detista_id", "estado_cita", "fecha_cita", "hora", "hora_fin");

    @Override
    public void migrate(Context context) throws Exception {
        for (String column : LEGACY_COLUMNS) {
            if (columnExists(context, column)) {
                context.getConnection().createStatement()
                        .execute("ALTER TABLE citas ALTER COLUMN " + column + " DROP NOT NULL");
            }
        }
    }

    private boolean columnExists(Context context, String column) throws SQLException {
        try (ResultSet columns = context.getConnection().getMetaData().getColumns(null, null, "CITAS", column.toUpperCase())) {
            if (columns.next()) {
                return true;
            }
        }
        try (ResultSet columns = context.getConnection().getMetaData().getColumns(null, null, "citas", column)) {
            return columns.next();
        }
    }
}
