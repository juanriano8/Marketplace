import java.sql.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Sincroniza la contraseña del administrador en la base de datos con la que
 * figura en la configuración. Necesario porque BootstrapAdminRunner solo crea
 * el usuario cuando no existe: si se cambia BOOTSTRAP_ADMIN_PASSWORD después
 * del primer arranque, la base conserva el hash antiguo.
 *
 * Uso: java AdminPasswordSync.java <host> <puerto> <usuario> <password> <base> <emailAdmin> <nuevaPassword>
 */
public class AdminPasswordSync {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://" + args[0] + ":" + args[1] + "/" + args[4]
            + "?sslmode=require&connectTimeout=15";

        String email = args[5].trim().toLowerCase();
        String newPassword = args[6];

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        try (Connection c = DriverManager.getConnection(url, args[2], args[3])) {

            // 1. Confirmar que existe y que el hash actual NO coincide ya
            String currentHash = null;
            try (PreparedStatement ps = c.prepareStatement(
                    "select password from users where email = ?")) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentHash = rs.getString(1);
                    }
                }
            }

            if (currentHash == null) {
                System.out.println("NO EXISTE el usuario " + email + " en la base.");
                return;
            }

            if (encoder.matches(newPassword, currentHash)) {
                System.out.println("SIN CAMBIOS: el hash almacenado ya corresponde a la contraseña indicada.");
                return;
            }

            System.out.println("El hash almacenado NO corresponde a la contraseña de la configuracion.");
            System.out.println("Generando hash nuevo y actualizando la fila...");

            // 2. Actualizar con un hash recien generado
            String newHash = encoder.encode(newPassword);
            try (PreparedStatement ps = c.prepareStatement(
                    "update users set password = ?, updated_at = now() where email = ?")) {
                ps.setString(1, newHash);
                ps.setString(2, email);
                int rows = ps.executeUpdate();
                System.out.println("Filas actualizadas: " + rows);
            }

            // 3. Verificar leyendo de nuevo desde la base
            try (PreparedStatement ps = c.prepareStatement(
                    "select password, enabled, role from users where email = ?")) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    String stored = rs.getString(1);
                    boolean enabled = rs.getBoolean(2);
                    String role = rs.getString(3);

                    System.out.println();
                    System.out.println("VERIFICACION (releida de la BD):");
                    System.out.println("   role    : " + role);
                    System.out.println("   enabled : " + enabled);
                    System.out.println("   matches : " + encoder.matches(newPassword, stored));
                }
            }
        }
    }
}
