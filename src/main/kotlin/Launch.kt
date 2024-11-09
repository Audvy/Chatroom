import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig

fun main() {
    lateinit var db: Database

    val hikariConfig: HikariConfig = HikariConfig().apply {
        val dbPort = 3306
        val dbHost = "44.203.47.64";
        val dbName = "chatroomdb"

        jdbcUrl = "jdbc:mariadb://$dbHost:$dbPort/$dbName"
        driverClassName = "org.mariadb.jdbc.Driver"
        username = "ubhack"
        password = "hacking"
    }

    val dbConfig = DatabaseConfig {
        useNestedTransactions = true;
    }

    val hikari: HikariDataSource = HikariDataSource(hikariConfig)
    db = Database.connect(datasource = hikari, databaseConfig = dbConfig)

    print("connected");
}