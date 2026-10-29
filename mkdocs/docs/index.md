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

> This plugin requires a MySQL database

1. Download lastest version of the plugin from Releases or from [Modrinth](https://modrinth.com/plugin/proxiesuite) (it is still under review for now).
2. Put the `.jar` file to your `plugins` folder of your Velocity server
3. Run the plugin, and change the configuration file in `plugins/proxiesuite/config.yaml`
4. Setup your MySQL database (please don't use root user!) in the `database` section
5. Setup your server names (main & login), or leave them if they are appropriate for you
6. That's it! VPN protection and login system is turned on by default!

<h2>Commands</h2>

<ul>
    <li>
        <code>/proxie</code>
        <ul>
            <li><code>ban</code>: Bans a player. Usage: <code>/proxie ban &lt;player&gt; &lt;reason&gt;</code></li>
            <li><code>tempban</code>: Temporarily bans a player. Usage: <code>/proxie tempban &lt;player&gt; &lt;digit&gt; &lt;unit&gt; &lt;reason&gt;</code></li>
            <li><code>pardon</code>: Unbans a player. Usage: <code>/proxie pardon &lt;player&gt;</code></li>
            <li><code>reload</code>: Reloads the configuration file.</li>
            <li><code>clientdump</code>: Dumps information the proxy has about a player.</li>
            <li><code>version</code>: Displays the plugin version.</li>
        </ul>
    </li>
    <li>
        <code>/login</code>
        <ul>
            <li><code>&lt;password&gt;</code>: Logs in using the specified password.</li>
        </ul>
    </li>
    <li>
        <code>/register</code>
        <ul>
            <li><code>&lt;password&gt; &lt;repeat that password&gt;</code>: Registers a user with the specified password.</li>
        </ul>
    </li>
    <li>
        <code>/premium</code>: Enables premium mode for players using an offline-mode account (Minecraft versions below 1.19.3).
    </li>
</ul>

<h2>Permissions</h2>

<ul>
    <li>
        <code>/proxie</code> command:
        <ul>
            <li><code>ban</code>: <code>proxiesuite.proxie.ban</code> (default: <code>false</code>)</li>
            <li><code>tempban</code>: <code>proxiesuite.proxie.tempban</code> (default: <code>false</code>)</li>
            <li><code>pardon</code>: <code>proxiesuite.proxie.pardon</code> (default: <code>false</code>)</li>
            <li><code>reload</code>: <code>proxiesuite.proxie.reload</code> (default: <code>false</code>)</li>
            <li><code>clientdump</code>: <code>proxiesuite.proxie.clientdump</code> (default: <code>false</code>)</li>
            <li><code>version</code>: <code>proxiesuite.proxie.version</code> (default: <code>true</code>)</li>
        </ul>
    </li>
</ul>

## Credits

Special thanks to: [josephrocca](https://github.com/josephrocca) for creating and maintaining [is-vpn](https://github.com/josephrocca/is-vpn.git) repo, allowing VPN detection (a list of ISP and suspicious VPN servers).

## License

ProxieSuite is licensed under MIT.
