package com.dp_ua.iksparser.bot.abilities.infoview;

import com.dp_ua.iksparser.dba.element.EventEntity;

public class BaseView {
    protected static String getLink(EventEntity event) {
        return !event.getResultUrl().isEmpty() ? event.getResultUrl()
                :
                !event.getStartListUrl().isEmpty()
                        ?
                        event.getStartListUrl()
                        :
                        "";
    }
}
