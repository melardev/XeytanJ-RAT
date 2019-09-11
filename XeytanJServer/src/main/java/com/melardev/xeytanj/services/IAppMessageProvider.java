package com.melardev.xeytanj.services;

import com.melardev.xeytanj.enums.Language;

public interface IAppMessageProvider extends IService {

    String getText(String key);

    void setLocaleForLanguage(Language lang);
}
