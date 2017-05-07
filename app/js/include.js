/**
 * Created by Romsahel on 24/03/2017.
 */
const http = require('http');
const https = require('https');
const ipcRenderer = require("electron").ipcRenderer;
const fs = require("fs");
const path = require("path");
const remote = require('electron').remote;
const {dialog, app, shell} = require('electron').remote;
const jsonfile = require('jsonfile');

const Cinema = require("./js/Cinema");
const MediaListManager = require("./js/MediaListManager");
const Configuration = require("./js/Configuration");
const SearchBar = require("./js/SearchBar");
const SplitBar = require("./js/SplitBar");
const InputHandler = require("./js/InputHandler");
const DropdownListHandler = require("./js/DropdownListHandler");
const MediaFinder = require("./js/MediaFinder");
const DetailHandler = require("./js/DetailHandler");
const Sorter = require("./js/Sorter");
const VLCRemote = require("./js/VLCRemote");
const VLCAdapter = require("./js/VLCAdapter");
