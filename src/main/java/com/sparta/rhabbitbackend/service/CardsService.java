package com.sparta.rhabbitbackend.service;

import com.sparta.rhabbitbackend.dto.CardsDetailDto;
import com.sparta.rhabbitbackend.dto.CardsRequestDto;
import com.sparta.rhabbitbackend.dto.CardsResponseDto;
import com.sparta.rhabbitbackend.model.Cards;
import com.sparta.rhabbitbackend.model.CardsDetail;
import com.sparta.rhabbitbackend.model.User;
import com.sparta.rhabbitbackend.repository.CardsDetailRepository;
import com.sparta.rhabbitbackend.repository.CardsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.smartcardio.Card;
import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CardsService {
    private final CardsRepository cardsRepository;
    private final CardsDetailRepository cardsDetailRepository;

    //메인화면-모든 카드 불러오기
    @Transactional
    public List<CardsResponseDto> viewAllCards(User user) {
        List<CardsResponseDto> cardsResponseDtoList = new ArrayList<>();

        Boolean isCard = cardsRepository.existsByUserId(user.getId());
        if (!isCard) {
            //throw new NullPointerException("게시물이 존재하지 않습니다.");
            //현재날짜 계산
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYYMMdd");
            String formatedNow = now.format(formatter);
            List<CardsDetailDto> cardsDetailDtoList = new ArrayList<>();
            //첫 카드 작성
            Cards cards = Cards.builder()
                    .user(user)
                    .date(formatedNow)
                    .build();
            cardsRepository.save(cards);
            //디테일 생성
            CardsDetailDto cardsDetailDto = CardsDetailDto.builder()
                    .textId(1L)
                    .text("첫 계획을 작성해 보세요")
                    .checked(false)
                    .daily(false)
                    .build();
            cardsDetailDtoList.add(cardsDetailDto);
            //내용 저장
            CardsDetail cardsDetail = CardsDetail.builder()
                    .cards(cards)
                    .user(user)
                    .text(cardsDetailDto.getText())
                    .checked(cardsDetailDto.getChecked())
                    .daily(cardsDetailDto.getDaily())
                    .build();
            cardsDetailRepository.save(cardsDetail);

            CardsResponseDto cardsResponseDto = CardsResponseDto.builder()
                    .cardsId(cards.getId())
                    .date(cards.getDate())
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .cardsDetailDtos(cardsDetailDtoList)
                    .build();

            cardsResponseDtoList.add(cardsResponseDto);
        } else {
            List<Cards> cardsList = cardsRepository.findAllByUserId(user.getId());
            for (Cards cards : cardsList) {
                List<CardsDetail> cardsDetailList = cardsDetailRepository.findAllByCardsId(cards.getId());  //한 카드당 디테일 정보 뽑아서
                List<CardsDetailDto> cardsDetailDtoList = new ArrayList<>();
                for (CardsDetail cardsDetail : cardsDetailList) {
                    CardsDetailDto cardsDetailDto = CardsDetailDto.builder()
                            .textId(cardsDetail.getId())
                            .daily(cardsDetail.getDaily())
                            .text(cardsDetail.getText())
                            .checked(cardsDetail.getChecked())
                            .build();
                    cardsDetailDtoList.add(cardsDetailDto);
                }
                CardsResponseDto cardsResponseDto = CardsResponseDto.builder()
                        .cardsId(cards.getId())
                        .date(cards.getDate())
                        .userId(user.getId())
                        .cardsDetailDtos(cardsDetailDtoList)
                        .nickname(user.getNickname())
                        .build();
                cardsResponseDtoList.add(cardsResponseDto);
            }
        }
        Collections.reverse(cardsResponseDtoList);
        return cardsResponseDtoList;
    }

    //디테일 화면
    @Transactional
    public CardsResponseDto viewCards(User user, Long cardId) {
        Cards cards = cardsRepository.findByUserIdAndId(user.getId(), cardId);

        List<CardsDetail> cardsDetailList = cardsDetailRepository.findAllByCardsId(cardId);
        List<CardsDetailDto> cardsDetailDtoList = new ArrayList<>();
        for (CardsDetail cardsDetail : cardsDetailList) {
            CardsDetailDto cardsDetailDto = CardsDetailDto.builder()
                    .textId(cardsDetail.getId())
                    .daily(cardsDetail.getDaily())
                    .text(cardsDetail.getText())
                    .checked(cardsDetail.getChecked())
                    .build();
            cardsDetailDtoList.add(cardsDetailDto);
        }
        CardsResponseDto cardsResponseDto = CardsResponseDto.builder()
                .cardsId(cards.getId())
                .date(cards.getDate())
                .nickname(user.getNickname())
                .userId(user.getId())
                .cardsDetailDtos(cardsDetailDtoList)
                .build();
        return cardsResponseDto;
    }

    // 디테일 리스트 추가 text, checked, daily
    @Transactional
    public CardsDetailDto createDetail(Long cardId, CardsRequestDto cardsRequestDto, User user) {
        Cards cards = cardsRepository.findById(cardId)
                .orElseThrow(() -> new NullPointerException("해당 카드가 존재하지 않습니다."));

        CardsDetail cardsDetail = CardsDetail.builder()
                .cards(cards)
                .user(user)
                .text(cardsRequestDto.getText())
                .checked(cardsRequestDto.getChecked())
                .daily(cardsRequestDto.getDaily())
                .build();
        cardsDetailRepository.save(cardsDetail);

        return CardsDetailDto.builder()
                .textId(cardsDetail.getId())
                .text(cardsDetail.getText())
                .checked(cardsDetail.getChecked())
                .daily(cardsDetail.getDaily())
                .build();
    }

    //업데이트
    @Transactional
    public void updateDetail(Long cardId, CardsDetailDto cardsDetailDto, User user) {
        CardsDetail cardsDetail = cardsDetailRepository.findById(cardsDetailDto.getTextId())
                .orElseThrow(() -> new NullPointerException("해당 리스트가 존재하지 않습니다."));

        cardsDetail.update(cardsDetailDto);
    }

    //삭제
    @Transactional
    public void deleteDetail(Long textId, Long cardsId) {
        cardsDetailRepository.deleteCardsDetailByIdAndCardsId(textId, cardsId);
    }

    //카드 추가 - 데일리 체크 목록만
    @Transactional
    public CardsResponseDto createCard(Long cardId, User user) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.KOREA);
        String today = sdf.format(date);

        //카드 생성
        Cards cards = Cards.builder()
                .date(today)
                .user(user)
                .build();
        cardsRepository.save(cards);
        //첫 내용 작성
        List<CardsDetail> yesterdayDetailList = cardsDetailRepository.findAllByCardsId(cardId);
        List<CardsRequestDto> cardsRequestDtos = new ArrayList<>();
        List<CardsDetailDto> cardsDetailDtos = new ArrayList<>();
        for (CardsDetail cardsDetail : yesterdayDetailList) {
            if (cardsDetail.getDaily()) {
                //어제 디테일 정보 가져오기(데일리 == true)
                CardsRequestDto cardsRequestDto = CardsRequestDto.builder()
                        .text(cardsDetail.getText())
                        .checked(cardsDetail.getChecked())
                        .daily(cardsDetail.getDaily())
                        .build();
                cardsRequestDtos.add(cardsRequestDto);

                //오늘 카드 디테일 생성
                CardsDetail newCardsDetail = CardsDetail.builder()
                        .cards(cards)
                        .user(user)
                        .text(cardsRequestDto.getText())
                        .checked(cardsRequestDto.getChecked())
                        .daily(cardsRequestDto.getDaily())
                        .build();
                cardsDetailRepository.save(newCardsDetail);

                //디테일 DTO 생성
                CardsDetailDto newCardsDetailDto = CardsDetailDto.builder()
                        .textId(newCardsDetail.getId())
                        .text(newCardsDetail.getText())
                        .checked(newCardsDetail.getChecked())
                        .daily(newCardsDetail.getDaily())
                        .build();
                cardsDetailDtos.add(newCardsDetailDto);
            }
        }

        //어제 카드가 모두 daily false 라면
        if (cardsDetailDtos.isEmpty()){
            CardsDetail CardsDetailEx = CardsDetail.builder()
                    .cards(cards)
                    .user(user)
                    .text("오늘도 화이팅")
                    .checked(false)
                    .daily(false)
                    .build();
            cardsDetailRepository.save(CardsDetailEx);

            //디테일 DTO 생성
            CardsDetailDto ExCardsDetailDto = CardsDetailDto.builder()
                    .textId(CardsDetailEx.getId())
                    .text(CardsDetailEx.getText())
                    .checked(CardsDetailEx.getChecked())
                    .daily(CardsDetailEx.getDaily())
                    .build();
            cardsDetailDtos.add(ExCardsDetailDto);
        }
        return CardsResponseDto.builder()
                .cardsId(cards.getId())
                .date(cards.getDate())
                .userId(user.getId())
                .nickname(user.getNickname())
                .cardsDetailDtos(cardsDetailDtos)
                .build();
    }
}