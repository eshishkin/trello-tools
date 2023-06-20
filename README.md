# Trello tools

Here is a simple set of tools for loading and parsing trello's backup files. And also for uploading data into 
external task managers (OpenProject, Obsidian) 

This project consist of several modules

- trello-downloader - uses Trello API to download data for selected boards and save it in json file (compatible with backup file provided by Trello itself via UI)
- trello-parser - picks up json files (from downloader), converts it into intermediate representation and save as another json file
- loader-obsidian - uses parsed data and converts it to a text file with markdown tasks
- loader-openproject - uses parsed data and loads that data to openproject instance