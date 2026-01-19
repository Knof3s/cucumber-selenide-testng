package helpers.reflects;

import helpers.Refl;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageContext {
    private static final ThreadLocal<PageContext> threadLocalContext = ThreadLocal.withInitial(PageContext::new);
    private Refl refl;

    private PageContext() {
        // Конструктор приватный, чтобы управлять доступом через ThreadLocal
    }

    public static PageContext getInstance() {
        return threadLocalContext.get();
    }
}
