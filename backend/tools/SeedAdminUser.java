import java.sql.*;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Crea (o restablece) una cuenta de administrador de prueba.
 *
 * No existe endpoint publico de registro de administradores, por eso este util
 * escribe directamente en la tabla `users` con un hash BCrypt generado por la
 * misma clase que usa la aplicacion, garantizando que el login funcione.
 *
 * Uso:
 *   java -cp "<jars>" tools/SeedAdminUser.java <host> <puerto> <usuario> <password> \
 *        <baseDeDatos> <emailAdmin> <passwordAdmin>
 */
public class SeedAdminUser {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://" + args[0] + ":" + args[1] + "/" + args[4]
            + "?sslmode=require&connectTimeout=15";

        String email = args[5].trim().toLowerCase();
        String plainPassword = args[6];

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        try (Connection c = DriverManager.getConnection(url, args[2], args[3])) {

            // 1. Comprobar columnas reales de la tabla (por si el esquema cambia)
            System.out.println("== Columnas de la tabla users ==");
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery(
                     "select column_name, data_type from information_schema.columns "
                   + "where table_schema='public' and table_name='users' order by ordinal_position")) {
                while (rs.next()) {
                    System.out.printf("   %-20s %s%n", rs.getString(1), rs.getString(2));
                }
            }

            // 2. Existe ya?
            String existingId = null;
            String existingRole = null;
            try (PreparedStatement ps = c.prepareStatement(
                    "select id::text, role from users where email = ?")) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) { existingId = rs.getString(1); existingRole = rs.getString(2); }
                }
            }

            String hash = encoder.encode(plainPassword);

            if (existingId != null) {
                // Idempotente: se deja la contrasena documentada siempre valida.
                try (PreparedStatement ps = c.prepareStatement(
                        "update users set password = ?, role = 'ROLE_ADMIN', enabled = true, "
                      + "seller_approved = null, updated_at = now() where email = ?")) {
                    ps.setString(1, hash);
                    ps.setString(2, email);
                    int rows = ps.executeUpdate();
                    System.out.println();
                    System.out.println("YA EXISTIA (rol anterior: " + existingRole + "). Filas actualizadas: " + rows);
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        "insert into users (id, email, password, role, enabled, seller_approved, "
                      + "created_at, updated_at, version) values (?, ?, ?, 'ROLE_ADMIN', true, null, now(), now(), 0)")) {
                    ps.setObject(1, UUID.randomUUID());
                    ps.setString(2, email);
                    ps.setString(3, hash);
                    ps.executeUpdate();
                    System.out.println();
                    System.out.println("CREADO el administrador " + email);
                }
            }

            // 3. Verificacion: releer de la base y comprobar el hash
            try (PreparedStatement ps = c.prepareStatement(
                    "select id::text, email, role, enabled, password from users where email = ?")) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    String stored = rs.getString(5);
                    System.out.println();
                    System.out.println("== VERIFICACION ==");
                    System.out.println("   id      : " + rs.getString(1));
                    System.out.println("   email   : " + rs.getString(2));
                    System.out.println("   role    : " + rs.getString(3));
                    System.out.println("   enabled : " + rs.getBoolean(4));
                    System.out.println("   password coincide : " + encoder.matches(plainPassword, stored));
                }
            }
        }
    }
}
