package com.bodimTikka.bodimTikka.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomPairBalanceDTO {
    private Long roomId;
    private Long fromUser;
    private Long toUser;
    private BigDecimal balance;

    public static List<RoomPairBalanceDTO> getRoomPairBalanceDTOS(List<Object[]> array) {
        return array.stream().map(obj -> new RoomPairBalanceDTO(
                ((Number) obj[0]).longValue(),
                ((Number) obj[1]).longValue(),
                ((Number) obj[2]).longValue(),
                new BigDecimal(obj[3].toString())
        )).toList();
    }
}
