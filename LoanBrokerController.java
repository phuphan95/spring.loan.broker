package org.springframework.integration.loanbroker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

@RestController
public class LoanBrokerController {
    public static final Logger LOGGER = LoggerFactory.getLogger(LoanBrokerController.class);
    private static final double BEST_QUOTE_VALUE = 0.04;
    private static final Timer timer = new Timer();
    @RequestMapping("/quotation-with-blocking")
    public ResponseEntity<?> quotation3(final @RequestParam(value = "loanAmount", required = true) Double loanAmount) throws Exception {


        return new ResponseEntity<Double>(bestQuotation(loanAmount), HttpStatus.OK);
    }

    @RequestMapping("/quotation-with-deferredresult")
    public DeferredResult<ResponseEntity<?>> quotation1(final @RequestParam(value = "loanAmount", required = true) Double loanAmount) throws Exception {

        final DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<ResponseEntity<?>>(5000l);
        deferredResult.onTimeout(new Runnable() {
            public void run() { // Retry on timeout
                deferredResult.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timeout occurred."));
            }
        });
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if(deferredResult.isSetOrExpired()){
//                    throw new RuntimeException();
//                }
//                else{
//                    try {
//                        Double bestQuotation = bestQuotation(loanAmount);
//                        deferredResult.setResult(ResponseEntity.ok(bestQuotation));
//                    }
//                    catch (Exception e){
//                        deferredResult.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e));
//                    }
//
//                }
//            }
//        },0);
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Double bestQuotation = bestQuotation(loanAmount);
                    deferredResult.setResult(ResponseEntity.ok(bestQuotation));
                } catch (Exception e) {
                    deferredResult.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e));
                }
            }
        });

        thread1.start();


        return deferredResult;
    }

    @RequestMapping("/quotation-with-callable")

    public Callable<Double> quotation2(final @RequestParam(value = "loanAmount", required = true) Double loanAmount) throws Exception {
        Callable<Double> callable = new Callable<Double>() {
            @Override
            public Double call() throws Exception {
                Double bestQuotation = bestQuotation(loanAmount);
                return bestQuotation;
            }
        };
        return callable;
    }

    Double bestQuotation(Double loanAmount) throws Exception {
        SettableListenableFuture<Double> a = new SettableListenableFuture<Double>();
        BankStub standardBank = new BankStub();
        standardBank.setInterestRate(0.05);
        standardBank.setBankType("Standard Bank");
        Double standardQuotation = standardBank.quotation(loanAmount);
        BankStub primeBank = new BankStub();
        primeBank.setInterestRate(0.04);
        primeBank.setBankType("Prime Bank");
        Double primeQuotation = primeBank.quotation(loanAmount);
        Double best_quotation = (standardQuotation > primeQuotation) ? primeQuotation : standardQuotation;
        a.set(best_quotation);
        return best_quotation;
    }
}