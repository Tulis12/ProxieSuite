<p align="center">
    <img src="img/logo.png" width="300">
</p>

# ProxieSuite

A handy plugin for your Velocity proxy: security, VPN detection, authentication, and automatic premium login!

## Features

- automatic premium login system, with fallback on offline-based logins
- VPN blocking and detection
- administration commands, such as `proxie ban`, `proxie tempban` and `proxie pardon`

## Setup

> [!NOTE]
> This plugin requires a MySQL database

1. Download lastest version of the plugin from Releases or from [Modrinth](https://modrinth.com/plugin/proxiesuite) (it is still under review for now).
2. Put the `.jar` file to your `plugins` folder of your Velocity server
3. Run the plugin, and change the configuration file in `plugins/proxiesuite/config.yaml`
4. Setup your MySQL database (please don't use root user!) in the `database` section
5. Setup your server names (main & login), or leave them if they are appropriate for you
6. That's it! VPN protection and login system is turned on by default!

## Commands

- `/proxie`
  - ban: bans a player; usage: `/proxie ban <player> <reason>`
  - tempban: tempbans a player; usage: `/proxie tempban <player> <digit> <unit> <reason>`
  - pardon: unbans a player; usage: `/proxie pardon <player>`
  - reload: reloads the config file
  - clientdump: dumps information the proxy has on a player
  - version: get plugin version
- `/login`
  - `<password>`: logs in with that password
- `/register`
  - `<password> <repeat that password>`: registers a user with set password
- `/premium`: enables premium mode when logged with offline-mode account (for players below 1.19.3)

## Permissions

- `/proxie` command:
  - ban: `proxiesuite.proxie.ban` (default false)
  - tempban: `proxiesuite.proxie.tempban` (default false)
  - pardon: `proxiesuite.proxie.pardon` (default false)
  - reload: `proxiesuite.proxie.reload` (default false)
  - clientdump: `proxiesuite.proxie.clientdump` (default false)
  - version: `proxiesuite.proxie.version` (default true)

## Credits

Special thanks to: [josephrocca](https://github.com/josephrocca) for creating and maintaining [is-vpn](https://github.com/josephrocca/is-vpn.git) repo, allowing VPN detection (a list of ISP and suspicious VPN servers).

## License

ProxieSuite is licensed under MIT.
