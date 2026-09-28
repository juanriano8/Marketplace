import java.sql.*;
import java.util.*;

/**
 * Borra los usuarios que crea la coleccion de Postman, para poder volver a
 * ejecutarla desde cero.
 *
 * La coleccion registra tres cuentas fijas (vendedor@, vendedor2@ y
 * comprador@marketplace.com). Si ya existen, el registro devuelve 400 y los
 * scripts de la coleccion se quedan sin token. Esta herramienta las elimina
 * junto con todo lo que dependa de ellas.
 *
 * Por defecto solo INFORMA (simulacion). Para borrar de verdad hay que pasar --apply.
 *
 * Uso:
 *   java -cp "<postgresql.jar>" tools/ResetPostmanUsers.java <host> <puerto> <user> <pass> <db>
 *   java -cp "<postgresql.jar>" tools/ResetPostmanUsers.java <host> <puerto> <user> <pass> <db> --apply
 */
public class ResetPostmanUsers {

    private static final String[] EMAILS = {
        "vendedor@marketplace.com",
        "vendedor2@marketplace.com",
        "comprador@marketplace.com"
    };

    public static void main(String[] args) throws Exception {
        boolean aplicar = Arrays.asList(args).contains("--apply");
        String url = "jdbc:postgresql://" + args[0] + ":" + args[1] + "/" + args[4]
            + "?sslmode=require&connectTimeout=15";

        try (Connection c = DriverManager.getConnection(url, args[2], args[3])) {
            c.setAutoCommit(false);

            List<String> ids = new ArrayList<>();
            System.out.println("Usuarios objetivo:");
            try (PreparedStatement ps = c.prepareStatement(
                    "select id::text, email, role from users where email = any(?)")) {
                Array arr = c.createArrayOf("text", EMAILS);
                ps.setArray(1, arr);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ids.add(rs.getString(1));
                        System.out.printf("   %-32s %-14s %s%n", rs.getString(2), rs.getString(3), rs.getString(1));
                    }
                }
            }

            if (ids.isEmpty()) {
                System.out.println("   (ninguno existe: nada que borrar)");
                return;
            }

            System.out.println();
            System.out.println("Dependencias encontradas:");
            int total = 0;
            total += informe(c, "reviews (como comprador)", "select count(*) from reviews where buyer_id = any(?)", ids);
            total += informe(c, "reviews (como vendedor)", "select count(*) from reviews where seller_id = any(?)", ids);
            total += informe(c, "cart_items (via carritos)", "select count(*) from cart_items ci join carts c on c.id = ci.cart_id where c.buyer_id = any(?)", ids);
            total += informe(c, "carts", "select count(*) from carts where buyer_id = any(?)", ids);
            total += informe(c, "order_items (via sub-ordenes)", "select count(*) from order_items oi join sub_orders so on so.id = oi.sub_order_id where so.seller_id = any(?)", ids);
            total += informe(c, "sub_orders (como vendedor)", "select count(*) from sub_orders where seller_id = any(?)", ids);
            total += informe(c, "orders (como comprador)", "select count(*) from orders where buyer_id = any(?)", ids);
            total += informe(c, "stock_movements", "select count(*) from stock_movements where seller_id = any(?)", ids);
            total += informe(c, "stock_items", "select count(*) from stock_items where seller_id = any(?)", ids);
            total += informe(c, "products", "select count(*) from products where seller_id = any(?)", ids);

            System.out.println();
            System.out.println("   filas dependientes en total: " + total);

            if (!aplicar) {
                System.out.println();
                System.out.println("SIMULACION: no se ha borrado nada.");
                System.out.println("Para borrar de verdad, anade --apply al final del comando.");
                c.rollback();
                return;
            }

            System.out.println();
            System.out.println("Borrando...");
            int borradas = 0;
            borradas += ejecutar(c, "delete from reviews where buyer_id = any(?)", ids);
            borradas += ejecutar(c, "delete from reviews where seller_id = any(?)", ids);
            borradas += ejecutar(c, "delete from cart_items where cart_id in (select id from carts where buyer_id = any(?))", ids);
            borradas += ejecutar(c, "delete from carts where buyer_id = any(?)", ids);
            borradas += ejecutar(c, "delete from order_items where sub_order_id in (select id from sub_orders where seller_id = any(?))", ids);
            borradas += ejecutar(c, "delete from sub_orders where seller_id = any(?)", ids);
            borradas += ejecutar(c, "delete from orders where buyer_id = any(?)", ids);
            borradas += ejecutar(c, "delete from stock_movements where seller_id = any(?)", ids);
            borradas += ejecutar(c, "delete from stock_items where seller_id = any(?)", ids);
            borradas += ejecutar(c, "delete from products where seller_id = any(?)", ids);

            try (PreparedStatement ps = c.prepareStatement("delete from users where email = any(?)")) {
                ps.setArray(1, c.createArrayOf("text", EMAILS));
                int n = ps.executeUpdate();
                System.out.println("   users eliminados: " + n);
                borradas += n;
            }

            c.commit();
            System.out.println();
            System.out.println("LISTO: " + borradas + " filas borradas (incluidos los usuarios).");
            System.out.println("La coleccion de Postman ya se puede ejecutar desde cero.");
        }
    }

    private static int informe(Connection c, String etiqueta, String sql, List<String> ids) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(conCast(sql))) {
            ps.setArray(1, c.createArrayOf("text", ids.toArray(new String[0])));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int n = rs.getInt(1);
                if (n > 0) {
                    System.out.printf("   %-32s %d%n", etiqueta, n);
                }
                return n;
            }
        }
    }

    private static int ejecutar(Connection c, String sql, List<String> ids) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(conCast(sql))) {
            ps.setArray(1, c.createArrayOf("text", ids.toArray(new String[0])));
            return ps.executeUpdate();
        }
    }

    /**
     * Las columnas son de tipo uuid y el arreglo se envia como text[], asi que
     * hay que convertir explicitamente para que PostgreSQL sepa compararlos.
     */
    private static String conCast(String sql) {
        return sql.replace("any(?)", "any(?::uuid[])");
    }
}
