# LoginSystem

![Java CI with Maven](https://github.com/BartuAbiHD/LoginSystem/actions/workflows/maven.yml/badge.svg)
[![SpigotMC Downloads](https://img.shields.io/spigot/dm/74894?label=SpigotMC%20Downloads)](https://www.spigotmc.org/resources/loginsystem-advanced-secure-auth-1-8-1-21-multi-db-api.74894/)
[![SpigotMC Rating](https://img.shields.io/spigot/r/74894?label=SpigotMC%20Rating)](https://www.spigotmc.org/resources/loginsystem-advanced-secure-auth-1-8-1-21-multi-db-api.74894/)
[![GitHub license](https://img.shields.io/github/license/BartuAbiHD/LoginSystem)](https://github.com/BartuAbiHD/LoginSystem/blob/main/LICENSE)

**A modern, secure, and highly customizable authentication plugin for Spigot/Paper servers [1.8 - 1.21+]**

LoginSystem is a high-performance authentication plugin designed from the ground up to maximize your server's security. All database operations run **asynchronously**, ensuring your server remains lag-free.

![Plugin Screenshot](https://i.ibb.co/FbMjhywp/Screenshot-422.png)
![Plugin Screenshot](https://i.ibb.co/0yGkwz2j/Screenshot-423.png)
![Plugin Screenshot](https://i.ibb.co/DDSbXdSS/Screenshot-424.png)

---

## ✅ Features

* **Wide Database Support:** Choose your preferred storage method!
    * File-based: `SQLite`, `H2`
    * SQL Servers: `MySQL`, `MariaDB`, `PostgreSQL`
    * NoSQL: `MongoDB`
* **Top-Tier Security:**
    * **SHA256** hashing by default.
    * Automatic & seamless migration from legacy hashes (`SHA1`, `MD5`).
    * Unsafe password prevention.
* **100% Translatable:**
    * All messages are managed through language files (`messages_en.yml`, `messages_tr.yml`, etc.).
    * Automatically downloads missing language files from GitHub.
* **Highly Customizable:**
    * Restrict movement, chat, and commands before login.
    * Set a login/register timeout to kick idle players.
    * Limit the number of registrations per IP address.
    * And many more options in a well-documented `config.yml`.
* **Full Suite of Commands:**
    * Essential player commands for registration and login.
    * Powerful admin commands to manage players, change passwords, and reload the plugin.
* **Developer API:**
    * A clean, powerful API to allow other plugins to integrate with LoginSystem.
    * Includes custom events for login, register, and more.
* **Automatic Update Checker:** Checks for new versions on SpigotMC and notifies admins.

---

## 🚀 Installation

1.  Download the latest version from the [SpigotMC page](https://www.spigotmc.org/resources/loginsystem-advanced-secure-auth-1-8-1-21-multi-db-api.74894/).
2.  Place the `LoginSystem.jar` file into your server's `/plugins` directory.
3.  Restart your server. The configuration files will be generated automatically.

---

## 💻 Commands & Permissions

### Player Commands
| Command                                      | Description                            | Permission      |
| -------------------------------------------- | -------------------------------------- | --------------- |
| `/login <password>`                          | Logs you into the server.              | `none`          |
| `/register <password> <confirmPassword>`     | Registers your account on the server.  | `none`          |
| `/changepassword <oldPassword> <newPassword>`| Changes your current password.         | `none`          |
| `/unregister <password>`                     | Deletes your registration.             | `none`          |
| `/logout`                                    | Securely logs you out.                 | `none`          |
| `/email <add\|remove> [email]`               | Manages your registered email address. | `none`          |

### Admin Commands (`/ls`)
| Command                               | Description                                      | Permission                      |
| ------------------------------------- | ------------------------------------------------ | ------------------------------- |
| `/ls help`                            | Displays the admin help menu.                    | `loginsystem.admin.help`        |
| `/ls reload`                          | Reloads the config and message files.            | `loginsystem.admin.reload`      |
| `/ls lang <language>`                 | Changes the plugin language and reloads.         | `loginsystem.admin.lang`        |
| `/ls register <player> <password>`    | Forcibly registers a player.                     | `loginsystem.admin.register`    |
| `/ls unregister <player>`             | Deletes a player's registration.                 | `loginsystem.admin.unregister`  |
| `/ls changepass <player> <newPassword>`| Changes a player's password.                   | `loginsystem.admin.changepass`  |
| `/ls forcelogin <player>`             | Forces an online player to log in.               | `loginsystem.admin.forcelogin`  |

> The parent permission `loginsystem.admin` grants access to all admin commands and is given to OPs by default.

---

## ⚙️ Configuration

The plugin is almost entirely customizable through the `config.yml` file.

<details>
<summary><b>Click to see a snippet of config.yml</b></summary>

```yaml
# Specify which method to use for data storage.
# Available options: sqlite, h2, mysql, mariadb, postgresql, mongodb
storage:
  type: sqlite

settings:
  # Specifies the language file to be used.
  # Example: en, tr, az, de
  # A list of all available languages can be found at: [https://github.com/BartuAbiHD/LoginSystem/tree/main/messages](https://github.com/BartuAbiHD/LoginSystem/tree/main/messages)
  messagesLanguage: "en"

  security:
    # Minimum password length.
    minPasswordLength: 6
    # The hashing algorithm to be used. BCRYPT is recommended.
    passwordHash: "SHA256"
    # Automatically update passwords from legacy hashes upon login.
    legacyHashes: []
      # - "SHA1"
    # Prevent unsafe (easily guessable) passwords from being used.
    unsafePasswords:
      - '123456'
      - 'password'
  
  restrictions:
    # Can unauthenticated players chat?
    allowChat: false
    # Maximum number of allowed registrations per IP (0 for unlimited).
    maxRegPerIp: 2
    # When enabled, an online player cannot be kicked due to "Logged in from another location".
    ForceSingleSession: true
    # After how many seconds should players who fail to login/register be kicked? (0 to disable)
    timeout: 30
# ... and many more settings!
```
</details>

---

## 👨‍💻 For Developers (API)

LoginSystem provides a powerful API to facilitate integration with other plugins.

### Add as a Dependency (Maven)
```xml
<repositories>
    </repositories>

<dependencies>
    <dependency>
        <groupId>me.bartuabihd</groupId>
        <artifactId>loginsystem</artifactId>
        <version>2.0-SNAPSHOT</version> <scope>provided</scope>
    </dependency>
</dependencies>
```

### Example Usage

Access the API:
```java
LoginSystemAPI api = LoginSystem.getApi();
```

Check if a player is logged in:
```java
if (api.isLoggedIn(player)) {
    // Player is authenticated
}
```

Listen for a successful login event:
```java
import me.bartuabihd.loginsystem.api.v2.event.LoginEvent;

@EventHandler
public void onPlayerAuthLogin(LoginEvent event) {
    Player player = event.getPlayer();
    player.sendMessage("Welcome back via API!");
}
```

---

## 🛠️ Building from Source

1.  Clone this repository: `git clone https://github.com/BartuAbiHD/LoginSystem.git`
2.  Navigate to the project directory: `cd LoginSystem`
3.  Build with Maven: `mvn clean install`

The final JAR will be located in the `/target` directory.

---

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.
