package com.br.naviv2.views;

import com.br.naviv2.NaviService;
import com.br.naviv2.views.components.NaviTextArea;
import com.br.naviv2.views.components.StyleSelector;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Route("")
public class HomeView extends VerticalLayout {

    private final NaviService naviService;
    private final TextArea originalTextArea = new NaviTextArea("", VaadinIcon.PENCIL.create());
    private final TextArea translatedTextArea = new NaviTextArea("", VaadinIcon.OPEN_BOOK.create());
    private final Select<String> selectStyle = new StyleSelector();

    public HomeView(NaviService naviService) {
        this.naviService = naviService;

        setSizeFull();

        selectStyle.setItems(
                "gíria das ruas",
                "criança de 2 anos",
                "Juridiquês",
                "Caipira",
                "Fausto Silva"
        );
        selectStyle.setPlaceholder("Selecione o estilo");

        translatedTextArea.setReadOnly(true);
        var split = new SplitLayout(originalTextArea, translatedTextArea);
        split.setWidthFull();
        split.setHeight("100%");

        var button = new Button("Traduzir", VaadinIcon.ARROW_RIGHT.create());
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        button.addClickListener(e -> handleTranslation(button));

        add(new H1("Navi"));
        add(new Paragraph("Tradutor de textos universais"));
        add(split);
        add(new HorizontalLayout(selectStyle, button));
    }

    private void handleTranslation(Button button) {
        String originalText = originalTextArea.getValue();
        String style = selectStyle.getValue();

        if (originalText == null || originalText.isBlank()) {
            Notification.show("Digite um texto antes de traduzir!", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        if (style == null || style.isBlank()) {
            Notification.show("Selecione um estilo de tradução!", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        // Limpa o campo de resposta e desabilita o botão durante o processamento
        translatedTextArea.setValue("");
        button.setEnabled(false);

        UI ui = UI.getCurrent();

        naviService.translateStream(originalText, style)
                .subscribe(
                        chunk -> {
                            if (ui != null) {
                                ui.access(() -> {
                                    String current = Objects.requireNonNullElse(translatedTextArea.getValue(), "");
                                    translatedTextArea.setValue(current + chunk);
                                });
                            }
                        },
                        error -> {
                            if (ui != null) {
                                ui.access(() -> {
                                    button.setEnabled(true);
                                    Notification.show("Erro na tradução: " + error.getMessage(), 5000, Notification.Position.MIDDLE)
                                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                                });
                            }
                        },
                        () -> {
                            if (ui != null) {
                                ui.access(() -> button.setEnabled(true));
                            }
                        }
                );
    }
}