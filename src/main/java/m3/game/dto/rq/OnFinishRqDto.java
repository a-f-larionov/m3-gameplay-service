package m3.game.dto.rq;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import m3.lib.dto.rq.UserIdRqDto;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OnFinishRqDto extends UserIdRqDto {
    @NotNull
    private Long pointId;
    @NotNull
    private Long score;
    @NotNull
    private Long chestId;
}
