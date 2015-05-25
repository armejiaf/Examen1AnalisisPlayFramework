/***************************************/
/*         intial setup                */
/***************************************/
var board = new Array(9);
var UID = "";
var socket;

function init() {

    /* use touch events if they're supported, otherwise use mouse events */
    var down = "mousedown";
    var up = "mouseup";
    if ('createTouch' in document) {
        down = "touchstart";
        up = "touchend";
    }

    /* add event listeners */
    document.querySelector("input.button").addEventListener(up, newGame, false);
    var squares = document.getElementsByTagName("td");
    for (var s = 0; s < squares.length; s++) {
        squares[s].addEventListener(down, function (evt) {
            squareSelected(evt, getCurrentPlayer());
        }, false);
    }

    createBoard();
    setInitialPlayer();
    socket = new WebSocket("ws://" + location.host + "/wsInterface");

    var writeData = function (event) {
        var todo = event.data;
        if (todo.length === 36 && UID === "") { //get UID from initial connection
            UID = todo;
        } else if (todo.length == 19 && (todo.indexOf(' ') != -1 || todo.indexOf('X') != -1 || todo.indexOf('O') != -1) && todo.indexOf(',') != -1) { //get updated game board for drawing purposes
            var elements = todo.split("~");
            updateBoard(elements[0])
            switchPlayers(elements[1]);
        }else{
            alert(todo);
        }
    }
    socket.onmessage = writeData;
}

function createBoard() {
    for (var i = 0; i < board.length; i++) {
        board[i] = "";
        document.getElementById(i).innerHTML = "";
    }
}

function squareSelected(evt, currentPlayer) {
    sendData(evt.target.id, currentPlayer);
}

function updateBoard(updatedBoard) {
    board = updatedBoard.split(",");
    for(var i = 0; i < 9; i++){
        document.getElementById(i).innerHTML = board[i];
    }
}

function getCurrentPlayer() {
    return document.querySelector(".current-player").id;
}

function setInitialPlayer() {
    var playerX = document.getElementById("X");
    var playerO = document.getElementById("O");
    playerO.className = "";
    playerX.className = "current-player";
}

function switchPlayers(current) {
    var playerX = document.getElementById("X");
    var playerO = document.getElementById("O");
    if(current.charAt(0) == 'X'){
        playerO.className = "current-player";
        playerX.className = "";
    }else{
        playerX.className = "current-player";
        playerO.className = "";
    }
}

function newGame() {
    socket.send("Restart~"+UID);
}

function sendData(square, currentPlayer) {
    var strInfo = square + "~" + currentPlayer + "~" + UID;
    socket.send(strInfo);
}

function closeConnection() {
    socket.send("Close~" + UID);
}