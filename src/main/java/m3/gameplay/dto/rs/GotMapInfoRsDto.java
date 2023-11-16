package m3.gameplay.dto.rs;

import lombok.*;
import lombok.experimental.SuperBuilder;
import m3.gameplay.dto.MapDto;
import m3.gameplay.dto.PointDto;
import m3.lib.dto.rs.UserIdRsDto;

import java.util.List;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GotMapInfoRsDto extends UserIdRsDto {

    private Long mapId;
    private MapDto map;
    private List<PointDto> points;
}
