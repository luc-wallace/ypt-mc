# YptMC

The Minecraft plugin for procrastinators with exams.

Compatible with Minecraft 1.21.4.

## Why?

Cause my A-level exams are coming up soon but I want to start a Minecraft server,
this is the only safe way of doing it.

## Usage

Place the jarfile (build in Releases tab) in your server plugins folder.

Upon first run, the config.yml file will be created in the plugin folder, with the below options:

```yml
token: JWT
group_id: 0
study_ratio: 3
```

`token`: YPT authentication token

`group_id`: ID of the YPT group to track

`study_ratio`: The number of minutes of studying required for one in game minute of play time

## Commands

`/ypt register <player> <ypt_id>`: Registers a new player on the server given a username and YPT ID (requires op)

`/ypt group`: Outputs all users in the YPT group and their IDs (requires op)

`/ypt time`: Outputs how much play time you have left on the server

`/ypt status`: Outputs status of all registered server members, whether they are on the server, studying or offline
