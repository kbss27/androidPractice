// IPlayService.aidl
package com.example.ch3_aidl;

//service쪽 패키지명과 같은 패키지명으로 작성해야함

// Declare any non-default types here with import statements
//Service쪽 app에서 .aidl(interface) 파일을 먼저 만들고
//service에서 이곳에 선언된 함수 구현..
//이 파일은 외부 app에게 파일로 공유되어야 함

//먼저 aidl 파일을 만들고 꼭 make module을 진행해야.. java 코드에서 인지가능
//build의 make module해야지 인지할 수 있음
interface IPlayService {
    //외부 app에서 aidl 방식으로 호출할 함수 나열..
    int currentPosition();
    int getMaxDuration();
    void start();
    void stop();
    int getMediaStatus();
}
