import java.sql.*;

/**
 * Informe del estado de la base de datos: tablas, columnas y numero de filas.
 * Util para revisar que hay en Cloud SQL sin abrir un cliente SQL.
 *
 * Uso:
 *   java -cp "<jar postgresql>" tools/DbReport.java <host> <puerto> <usuario> <password> <base>
 */
public class DbReport {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://" + args[0] + ":" + args[1] + "/" + args[4]
            + "?sslmode=require&connectTimeout=15";

        try (Connection c = DriverManager.getConnection(url, args[2], args[3])) {
            System.out.println("Base de datos : " + args[4]);
            System.out.println("Servidor      : " + c.getMetaData().getDatabaseProductVersion());
            System.out.println();

            System.out.printf("%-20s %8s %10s  %s%n", "TABLA", "COLUMNAS", "FILAS", "COLUMNAS (nombres)");
            System.out.println("-".repeat(120));

            int totalTablas = 0;
            long totalFilas = 0;

            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(
                     "select table_name from information_schema.tables "
                   + "where table_schema='public' and table_type='BASE TABLE' order by table_name")) {

                java.util.List<String> tablas = new java.util.ArrayList<>();
                while (rs.next()) {
                    tablas.add(rs.getString(1));
                }

                for (String tabla : tablas) {
                    java.util.List<String> cols = new java.util.ArrayList<>();
                    try (PreparedStatement ps = c.prepareStatement(
                            "select column_name from information_schema.columns "
                          + "where table_schema='public' and table_name=? order by ordinal_position")) {
                        ps.setString(1, tabla);
                        try (ResultSet crs = ps.executeQuery()) {
                            while (crs.next()) {
                                cols.add(crs.getString(1));
                            }
                        }
                    }

                    long filas = -1;
                    try (Statement cst = c.createStatement();
                         ResultSet frs = cst.executeQuery("select count(*) from " + tabla)) {
                        frs.next();
                        filas = frs.getLong(1);
                    } catch (SQLException e) {
                        // se deja -1 si no se puede contar
                    }

                    totalTablas++;
                    if (filas > 0) {
                        totalFilas += filas;
                    }

                    String colList = String.join(", ", cols);
                    if (colList.length() > 58) {
                        colList = colList.substring(0, 55) + "...";
                    }
                    System.out.printf("%-20s %8d %10d  %s%n", tabla, cols.size(), filas, colList);
                }
            }

            System.out.println("-".repeat(120));
            System.out.printf("TOTAL: %d tablas, %d filas sumadas%n", totalTablas, totalFilas);

            // Desglose de usuarios por rol
            System.out.println();
            System.out.println("== Usuarios por rol ==");
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(
                     "select role, count(*) as n, "
                   + "sum(case when enabled then 1 else 0 end) as habilitados, "
                   + "sum(case when seller_approved then 1 else 0 end) as verificados "
                   + "from users group by role order by role")) {
                while (rs.next()) {
                    System.out.printf("   %-14s total=%-4d habilitados=%-4d verificados=%d%n",
                        rs.getString(1), rs.getInt(2), rs.getInt(3), rs.getInt(4));
                }
            }
        }
    }
}
