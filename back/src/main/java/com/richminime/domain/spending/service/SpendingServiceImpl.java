package com.richminime.domain.spending.service;

import com.richminime.domain.spending.constatnt.SpendingCategoryEnums;
import com.richminime.domain.spending.constatnt.TimeEnums;
import com.richminime.domain.spending.domain.MonthSpendingPattern;
import com.richminime.domain.spending.domain.Spending;
import com.richminime.domain.spending.dto.response.FindMonthSpendingResDto;
import com.richminime.domain.spending.dto.response.SpendingDto;
import com.richminime.domain.spending.repository.MonthSpendingPatternRedisRepository;
import com.richminime.domain.spending.repository.SpendingRepository;
import com.richminime.domain.user.domain.User;
import com.richminime.domain.user.exception.UserExceptionMessage;
import com.richminime.domain.user.exception.UserNotFoundException;
import com.richminime.domain.user.repository.UserRepository;
import com.richminime.global.common.codef.CodefWebClient;
import com.richminime.global.common.codef.dto.request.FindSpendingListReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class SpendingServiceImpl implements SpendingService {

    private final SpendingRepository spendingRepository;
    private final UserRepository userRepository;
    private final CodefWebClient codefWebClient;

    // redis에 각 회원의 월별 소비패턴 정보를 저장함
    private final MonthSpendingPatternRedisRepository monthSpendingPatternRedisRepository;

    /**
     * 회원들의 소비내역을 불러와 spending에 저장
     * @throws UnsupportedEncodingException
     */
    @Override
    public void addSpending(User user, String startDate, String endDate) {
        // codef api 호출
        // 리스트로 받아서 한꺼번에 저장
        try {
            spendingRepository.saveAll(codefWebClient.findSpendingList(FindSpendingListReqDto.create(user, startDate.toString(), endDate.toString()), user.getUserId()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 월별 소비패턴 분석한 값을 반환
     * @return
     */
    @Override
    public FindMonthSpendingResDto findMonthSpending() {
        String email = getLoginId();
        Map<String, Long> spendingAmountMap;
        // 이미 분석해서 redis에 저장해놓은 상태면 반환함
        Optional<MonthSpendingPattern> monthSpendingPattern = monthSpendingPatternRedisRepository.findById(email);
        // date로 캘린더 생성
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        // 년, 월 값 가져오기
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        if(!monthSpendingPattern.isPresent()) {
            // redis에 저장된 데이터가 없으면 분석해서 새로 redis에 저장함
            User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(UserExceptionMessage.USER_NOT_FOUND.getMessage()));
            // 중복된 코드가 너무 많음 -> 코드 리팩토링 필요,,,
            // 해당 년 월의 마지막 날을 가져오기
            YearMonth yearMonth = YearMonth.of(year, month);
            int lastDay = yearMonth.lengthOfMonth();
            StringBuilder startDate = new StringBuilder();
            StringBuilder endDate = new StringBuilder();
            // 달이 10 미만이라면 앞에 0을 붙여줘야 함
            if(month < 10) {
                startDate.append(year).append(0).append(month).append("01");
                endDate.append(year).append(0).append(month).append(lastDay);
            }else {
                startDate.append(year).append(month).append("01");
                endDate.append(year).append(month).append(lastDay);
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            try {
                updateMonthSpending(user, month, sdf.parse(startDate.toString()), sdf.parse(endDate.toString()));
                monthSpendingPattern = monthSpendingPatternRedisRepository.findById(email);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        spendingAmountMap = monthSpendingPattern.get().getSpendingAmountMap();

        // 모든 키-값 쌍 가져오기
        Set<Map.Entry<String, Long>> entrySet = spendingAmountMap.entrySet();
        List<SpendingDto> spendingAmountList = new ArrayList<>();
        String key;
        // 카테고리 별 합계 정보를 DTO 형태로 변환
        for(Map.Entry<String, Long> entry : entrySet) {
            key = entry.getKey();
            if(key.equals(SpendingCategoryEnums.INTERNET_PG.getCategory()))
                key = SpendingCategoryEnums.INTERNET_PG.getValue();
            spendingAmountList.add(SpendingDto.builder()
                    .category(key)
                    .amount(entry.getValue())
                    .build());
        }
        // 응답 객체 반환
        return FindMonthSpendingResDto.builder()
                .month(month)
                .spendingAmountList(spendingAmountList)
                .totalAmount(monthSpendingPattern.get().getTotalAmount())
                .build();
    }

    /**
     * 소비패턴 내역에서 업종 별로 사용 합계를 계산함
     * @param spendingList
     */
    @Override
    public MonthSpendingPattern analyzeMonthSpending(List<Spending> spendingList, String email, int month) {
        // 카테고리 별 합계 저장
        Map<String, Long> map = new HashMap<>();
        String category; // 소비 유형
        Long totalAmount = 0L;
        Long amount;
        for (Spending spending : spendingList) {
            category = spending.getCategory();
            amount = map.get(category);
            // map에 저장되어 있지 않은 카테고리인 경우 초기값 0으로 초기화
            if(amount == null) amount = 0L;
            amount += spending.getCost();
            totalAmount += spending.getCost();
            map.put(category, amount);
        }
        return MonthSpendingPattern.builder()
                .email(email)
                .month(month)
                .spendingAmountMap(map)
                .totalAmount(totalAmount)
                .expiration(getMonthExpritation())
                .build();
    }

    @Override
    public void analyzeDaySpending() {

    }

    /**
     * 해당 회원의 월 소비 패턴을 갱신 (매달 1일마다 갱신됨)
     * @param user
     * @param startDate
     * @param endDate
     */
    @Override
    public void updateMonthSpending(User user, int month, Date startDate, Date endDate) {
        List<Spending> spendingList = spendingRepository.findSpendingByUserIdAndSpentDateBetween(user.getUserId(), startDate, endDate);
        MonthSpendingPattern monthSpendingPattern = analyzeMonthSpending(spendingList, user.getEmail(), month);
        monthSpendingPatternRedisRepository.save(monthSpendingPattern);
    }

    /**
     * 현재 달의 마지막날 까지가 redis에 존재하는 유효기간
     * 그 값을 반환
     * @return
     */
    private Long getMonthExpritation(){
        // date로 캘린더 생성
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);

        // 년, 월 값 가져오기
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        // 해당 년 월의 마지막 날을 가져오기
        YearMonth yearMonth = YearMonth.of(year, month);
        int lastDay = yearMonth.lengthOfMonth();

        // 한 달의 일수 * 1일 시간단위
        return lastDay * TimeEnums.DAY_TIME_VALUE.getValue();
    }

    /**
     * 현재 로그인한 회원 아이디(이메일)을 반환
     */
    private String getLoginId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        return principal.getUsername();
    }


}