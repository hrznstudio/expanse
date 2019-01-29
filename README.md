![alt tag](docs/expanse.png)

# Horizon Expanse

[![Discord](https://img.shields.io/discord/422424112863117312.svg?style=for-the-badge&logo=discord)](https://discord.gg/758eCD7)
![](https://img.shields.io/github/contributors/HRZNStudio/Expanse.svg?style=for-the-badge&logo=github)
![](https://img.shields.io/github/issues/HRZNStudio/Expanse.svg?style=for-the-badge&logo=github)
![](https://img.shields.io/github/issues-pr/HRZNStudio/Expanse.svg?style=for-the-badge&logo=github)
![](https://img.shields.io/github/forks/HRZNStudio/Expanse.svg?style=for-the-badge&logo=github)
![](https://img.shields.io/github/stars/HRZNStudio/Expanse.svg?style=for-the-badge&logo=github)
![](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)

## What is it?
Expanse is a project which aims to completely replace minecraft's networking internals with SpatialOS to allow infinite worlds with near infinite people.

## How does it work?

The traditional ways to develop large online games mean that you’re either limited by the capacity of a single game server, or you have to shard your game world.
![alt tag](docs/trad-client-server.png)
SpatialOS works differently: it brings together many servers so they’re working as one. But it does this in a way that makes a single world which looks seamless to players.
![alt tag](docs/deployment.png)

## How can I contribute? [![Open Source Helpers](https://www.codetriage.com/hrznstudio/spatial/badges/users.svg)](https://www.codetriage.com/hrznstudio/spatial)

### Prerequisites
- [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- SpatialOS CLI [Windows](https://docs.improbable.io/reference/13.5/shared/get-started/setup/win) [MacOS](https://docs.improbable.io/reference/13.5/shared/get-started/setup/mac) [Linux](https://docs.improbable.io/reference/13.5/shared/get-started/setup/linux)

### Hardware Requirements
|           | Minimum                           | Recommended                              |
|-----------|-----------------------------------|------------------------------------------|
| Processor | i5                                | i7                                       |
| Memory    | 8GB                               | 16GB                                     |
| Network   | Any broadband internet connection | High-speed broadband internet connection |
| Storage   | 12GB available space              | 12GB available space                     |

### Set up environment

> Any missing information will be found [here](https://docs.improbable.io/reference/13.5/shared/build)

run `spatial worker build` to set up the worker SDKs
