package vn.shopee.shopeeproject.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import vn.shopee.shopeeproject.annotation.MyLog;

@Component
@Aspect
@Slf4j
public class LoggingAspect {

    // @Around указывает, что код ниже "обернет" вызываемый метод.
    // В скобках мы пишем специальное правило: перехватывать методы,
    // над которыми стоит аннотация @MyLog (указываем полный путь к ней)
    @Around("@annotation(mylog)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint,MyLog mylog) throws Throwable {

        // 1. Код ДО выполнения метода
        long startTime = System.currentTimeMillis();

        // Получаем имя метода, который вызвал пользователь, для красивого лога
        String methodName = joinPoint.getSignature().toShortString();
        log.info(">>>> [Лог] Начало выполнения метода: {}", methodName);

        // 2. ЗАПУСК самого метода
        // Этот вызов заставляет выполниться ваш оригинальный метод в контроллере/сервисе.
        // Результат работы метода мы сохраняем в переменную result
        Object result = joinPoint.proceed();

        // 3. Код ПОСЛЕ выполнения метода
        long totalTime = System.currentTimeMillis() - startTime;
        log.info("<<<< [Лог] Метод {} успешно завершился за {} мс", methodName, totalTime);

        // Обязательно возвращаем результат работы оригинального метода дальше,
        // иначе тот, кто вызывал метод, ничего не получит в ответ
        return result;
    }
}
