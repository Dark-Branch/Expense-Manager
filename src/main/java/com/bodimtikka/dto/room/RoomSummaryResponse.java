package com.bodimtikka.dto.room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RoomSummaryResponse {
    private Long id;
    private String name;
    List<ParticipantSummary> participants;
}
