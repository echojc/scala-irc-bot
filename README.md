scala-irc-bot
========

A fork of [scala-irc-bot](https://github.com/scala-irc-bot/scala-irc-bot).

## Usage

This project is intended to be included as a library dependency.

Configure the IRC client via `application.conf`. These are the default values:

```
scala-irc-bot {
  hostname = "localhost"
  port = 6667
  password = ""

  # the list of channels to join, each prefixed with '#'
  channels = []

  nickname = "scala-irc-bot"
  username = ${scala-irc-bot.nickname}
  realname = ${scala-irc-bot.nickname}

  # add your bots here (see next section in README)
  bots {
    sh-echo-SystemBot {
      command-prefix = "!sys:"
      shutdown-enabled = false
    }
  }

  encoding = "utf-8"

  # how long to wait before sending when invoking `sendMessage`
  message-delay = 0

  # how long between each `onTimer` event
  timer-delay = 60000
}
```

## Adding bots

Bots should be placed in the `bots` key of your configuration. There exists a
system bot by default; you do not need to copy this into your local config
because config objects are merged (only values are overidden).

Each key under the `bots` key should be the fully-qualified name of your bot.
Replace the dots ('.') in the class name with hyphens ('-') since dots are
reserved characters in the config file.

Each bot must have a config object as its value. This may be an empty object
or it can contain additional values that are passed to your bot when it is
created. Here is an example configuration:

```
bots {
  # a complex bot with several config values
  sh-echo-ComplexBot {
    some-key = "123"
    another-key = true
  }
  # an echobot with no configuration
  sh-echo-EchoBot {}
}
```

The config object for each bot is detached as its own `Config` object so key
names are relative to each bot's own configuration block. For example, to
access the `some-key` key within `ComplexBot`, you should use:

```
config.getString("some-key")
```

as opposed to the absolute path (`scala-irc-bot.bots.sh-echo-ComplexBot.some-key`).

Bots should implement the `net.mtgto.irc.Bot` trait. Every method has a default
implementation so you only need to implement the events you require. As for
constructors, you have a choice between two:

```
class MyBot extends Bot
class MyBot(config: Config) extends Bot
```

If your bot does not require any configuration, you can use the first
constructor. The dynamic bot loader will call the constructor with the `Config`
parameter if it exists, otherwise it falls back to the default constructor.


## SystemBot

A basic bot is included. It has two commands:

- `!sys:reload` - dynamically reloads bots from your configuration
- `!sys:shutdown` - shuts down the client if enabled in the configuration

This bot can be configured; see the reference configuration for more details.

## License
scala-irc-bot is licensed under the GNU GPL v3.

