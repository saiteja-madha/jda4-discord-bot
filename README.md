# JDA 4 Discord Bot
A cool multipurpose bot built using Java Discord API https://github.com/DV8FromTheWorld/JDA

## Available Commands
### Utility Commands:
Command | Description
------------ | -------------
`!covid <country>` | Get covid statistics in the specified country
`!github <username>` | Shows github statistics of a user
`!haste <text>` | Posts some text to hastebin
`!help <command>` | Shows the list with commands in the bot
`!translate <code> <text>` | Translate from one language to other
`!trcodes` | Displays a list of available translate codes
`!urban <search-term>` | Searches the urban dictionary

### Fun Commands:
Command | Description
------------ | -------------
`!animal <name>` | Show a random image of selected animal types
`!cat` | Shows a random cat image
`!dog` | Shows a random dog image
`!joke` | Shows a random joke
`!flipcoin` | Flips a coin heads or tails
`!fliptext` | Reverses the given message
`!meme` | Shows a random meme

### Information Commands
Command | Description
------------ | -------------
`!avatar` | Displays avatar information about the user
`!botinfo` | Shows bot information
`!channelinfo [#channel]` | Shows mentioned channel information
`!guildinfo` | Shows information about the discord server
`!invite` | Get the bot's invite
`!ping` | Shows the current ping from the bot to the discord servers
`!roleinfo` | Shows information of the specified role
`!uptime` | Shows bot's uptime
`!userinfo` | Shows information about the user

### Image Commands
#### Image Filters
<table>
   <tr>
      <td>blur</td>
      <td>contrast</td>
      <td>gay</td>
      <td>greyscale</td>
      <td>invert</td>
      <td>sepia</td>
   </tr>
</table>

#### Image Generators
<table>
   <tr>
      <td>ad</td>
      <td>affect</td>
      <td>approved</td>
      <td>batslap</td>
      <td>beautiful</td>
   </tr>
   <tr>
      <td>bed</td>
      <td>delete</td>
      <td>discordblack</td>
      <td>discordblue</td>
      <td>doublestonk</td>
   </tr>
   <tr>
      <td>facepalm</td>
      <td>frame</td>
      <td>hitler</td>
      <td>jail</td>
      <td>karaba</td>
   </tr>
   <tr>
      <td>kiss</td>
      <td>mms</td>
      <td>notstonk</td>
      <td>podium</td>
      <td>poutine</td>
   </tr>
   <tr>
      <td>rejected</td>
      <td>rip</td>
      <td>spank</td>
      <td>stonk</td>
      <td>tatoo</td>
   </tr>
   <tr>
      <td>trash</td>
      <td>wanted</td>
   </tr>
</table>

#### Text Generators
<table>
   <tr>
      <td>achievement</td>
      <td>belikebill</td>
      <td>presentation</td>
   </tr>
</table>

### Moderation Commands

Command | Description
------------ | -------------
`!ban <@member(s)> [reason]` | Ban a member off the server
`!kick <@member(s)> [reason]` | Kick a member off the server
`!kick <@member(s)> [reason]` | Kick a member off the server
`!purgeattach <amount>` | Deletes the specified amount of messages with attachments
`!purgebots <amount>` | Deletes the specified amount of messages from bots
`!purge <amount>` | Deletes the specified amount of messages
`!purgelinks <amount>` | Deletes the specified amount of messages with links
`!purgeuser <@user> <amount>` | Deletes the specified amount of messages for the mentioned user
`!softban <@member(s)> [reason]` | Kicks a member from the server and delete that users messages
