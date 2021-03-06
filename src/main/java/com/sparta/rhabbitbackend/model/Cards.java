package com.sparta.rhabbitbackend.model;

import com.sparta.rhabbitbackend.dto.CardsDetailDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Cards {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn
    private User user;

    @Column
    private String date;           //월 / 일 / 시간 / 분

    @Builder
    public Cards(User user, String date){
    this.user = user;
    this.date = date;
    }

}
