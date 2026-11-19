package br.ufrn.dimap.models;

import java.util.UUID;

public record User(
        int id, String name, int age
) {

}