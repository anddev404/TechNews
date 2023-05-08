package com.anddev404.technews.mainActivity

/**
 * Enumeracja "FragmentsEnum" zawiera wartości odpowiadające etapom działania aplikacji:
 * LOAD_LIST - reprezentuje etap pobierania listy aktualności z internetu.
 * ERROR - reprezentuje etap wyświetlania błędu, gdy nie uda się pobrać listy aktualności z internetu.
 * SHOW_LIST - reprezentuje etap wyświetlania listy pobranych aktualności.
 * SHOW_NEWS_DETAILS - reprezentuje etap wyświetlania szczegółów wybranej aktualności.
 */
enum class FragmentsEnum {
    LOAD_LIST, ERROR, SHOW_LIST, SHOW_NEWS_DETAILS
}