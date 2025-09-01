package com.bodimtikka.dto.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private String name;
    private UserSummary owner;
    private List<UserSummary> participants;
}
