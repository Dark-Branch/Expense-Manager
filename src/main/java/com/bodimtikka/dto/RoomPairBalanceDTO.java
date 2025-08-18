package com.bodimtikka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomPairBalanceDTO {
    private UUID roomId;
    private UUID fromUser;
    private UUID toUser;
    private BigDecimal balance;

    public static List<RoomPairBalanceDTO> getRoomPairBalanceDTOS(List<Object[]> array) {
        return array.stream().map(obj -> new RoomPairBalanceDTO(
                (UUID) obj[0],
                (UUID) obj[1],
                (UUID) obj[2],
                new BigDecimal(obj[3].toString())
        )).toList();
    }
}
