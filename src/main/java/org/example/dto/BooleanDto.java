package org.example.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BooleanDto {

    Boolean answer;

    public static BooleanDto makeDefault(Boolean answer) {
        return builder()
                .answer(answer)
                .build();
    }
}
