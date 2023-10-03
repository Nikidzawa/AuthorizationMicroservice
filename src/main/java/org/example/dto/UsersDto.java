package org.example.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UsersDto {
        @NonNull
        Long id;

        @NonNull
        String name;

        @NonNull
        String email;

        @NonNull
        String password;
}
