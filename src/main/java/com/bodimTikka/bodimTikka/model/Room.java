package com.bodimTikka.bodimTikka.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Room {
    private String id;
    private String name;
    private List<Roomer> roomers;
}
