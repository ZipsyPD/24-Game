# Multiplayer 24-Game
Multiplayer 24-Game (Create 24 with operations (+, -, /, *) and 4 given numbers! )

Every game is possible and requires four players! 

## Java 8 Setup

The Swing client requires Java 8 because it uses GlassFish 5 client libraries.
Docker desktop is also needed for docker running - there are legacy scripts as well that can be modified and ran if glassfish has already been installed. 

### macOS

#### How to install Java 8 if not installed

Using Homebrew:

```bash
brew install --cask temurin@8
```

You can temporarily switch to java 8 with this:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
export PATH="$JAVA_HOME/bin:$PATH"
```

### Linux

#### How to install(ubuntu and debian and if it's not provided use Eclipse Temurin 8)

```bash
sudo apt update
sudo apt install openjdk-8-jdk
```

You can switch with these steps:
```bash
update-alternatives --list java
// Then put that path in $FOUND_PATH$
export JAVA_HOME=$FOUND_PATH$
export PATH="JAVA_HOME/bin:$PATH"
```

### Windows

#### How to install (using eclipse temurin 8)

You can install eclipse temurin 8 from the eclipse adoptium java 8 distribution. 
After installing find the path which could be something like
```bash
C:\Program Files\Eclipse Adoptium\jdk-8.x.x
```
After finding your path do this to temporarily swithc:
```bash
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-8.x.x"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```


After you have switched to java 8 you are ready to run the program. To do this go to the project root and run 
```bash
docker compose up
```

After that server is running, we can open more clients. 
First build the files using:
```bash
./scripts/compile.sh
```
And then run a client using:
```bash
./scripts/run-client.sh
```


## Developer notes

I wanted to make this project as a distributed systems exercise 

There are improvements that need to be made in regards to legacy. Of course we use these technologies in our classes but they're outdated. Networking is also currently intended only for same host application. I can choose to run this on a hosted server using something free, but that's not necessary I'm sure there's others that are better for this. 

If there is a need for multiplayer online though what you would have to do is 
- Run the backend on public and give it a public hostname
- Change all the locahosts to that hostname
- Make sure the needed ports are exposed through firewall(so other people can reach it)
- Make RMI advertise public hostname as well
- Make sure Glassfish also advertises those same addresses
- Probably add authentication! 
In this case there is a way better for making it public. 

As for the game itself. An elo system and matchmaking system would be really cool to make. 

Toodles! 
