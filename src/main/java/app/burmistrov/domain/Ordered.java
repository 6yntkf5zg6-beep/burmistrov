package app.burmistrov.domain;

/**
 * Запись, у которой порядок в списке задаёт человек, а не данные.
 * Нужна, чтобы перестановка и перенумерация были написаны один раз на все такие списки.
 */
public interface Ordered {

    Long getId();

    Integer getSortOrder();

    void setSortOrder(Integer sortOrder);
}
