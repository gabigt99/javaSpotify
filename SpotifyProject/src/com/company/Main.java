package com.company;

import bg.sofia.uni.fmi.mjt.spotify.server.Server;

public class Main {

    public static void main(String[] args) {
        Server server = new Server(6600);
        server.start();
    }
}
