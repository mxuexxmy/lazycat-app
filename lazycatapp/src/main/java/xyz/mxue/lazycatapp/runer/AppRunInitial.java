package xyz.mxue.lazycatapp.runer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import xyz.mxue.lazycatapp.sync.AppRunInitialService;

@Component
@RequiredArgsConstructor
public class AppRunInitial implements ApplicationRunner {

    private final AppRunInitialService appRunInitialService;

    @Override
    public void run(ApplicationArguments args) {
        appRunInitialService.sync();
    }
}
