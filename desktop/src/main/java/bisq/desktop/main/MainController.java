/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.desktop.main;

import bisq.common.observable.Pin;
import bisq.common.observable.collection.CollectionObserver;
import bisq.desktop.ServiceProvider;
import bisq.desktop.common.view.Controller;
import bisq.desktop.common.view.NavigationController;
import bisq.desktop.common.view.NavigationTarget;
import bisq.desktop.components.overlay.Popup;
import bisq.desktop.main.content.ContentController;
import bisq.desktop.main.left.LeftNavController;
import bisq.desktop.main.top.TopPanelController;
import bisq.support.alert.AlertService;
import bisq.support.alert.AuthorizedAlertData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class MainController extends NavigationController {
    @Getter
    private final MainModel model = new MainModel();
    @Getter
    private final MainView view;
    private final ServiceProvider serviceProvider;
    private final LeftNavController leftNavController;
    private final AlertService alertService;
    private Pin alertsPin;

    public MainController(ServiceProvider serviceProvider) {
        super(NavigationTarget.MAIN);

        this.serviceProvider = serviceProvider;

        alertService = serviceProvider.getSupportService().getAlertService();

        leftNavController = new LeftNavController(serviceProvider);
        TopPanelController topPanelController = new TopPanelController(serviceProvider);

        view = new MainView(model,
                this,
                leftNavController.getView().getRoot(),
                topPanelController.getView().getRoot());
    }

    @Override
    public void onActivate() {
        alertsPin = alertService.getAlerts().addListener(new CollectionObserver<>() {
            @Override
            public void add(AuthorizedAlertData element) {
                new Popup().attention(element.getMessage()).show();
            }

            @Override
            public void remove(Object element) {

            }

            @Override
            public void clear() {

            }
        });
    }

    @Override
    public void onDeactivate() {
        alertsPin.unbind();
    }

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    protected Optional<? extends Controller> createController(NavigationTarget navigationTarget) {
        switch (navigationTarget) {
            case CONTENT: {
                return Optional.of(new ContentController(serviceProvider));
            }
            default: {
                return Optional.empty();
            }
        }
    }

    @Override
    public void onStartProcessNavigationTarget(NavigationTarget navigationTarget, Optional<Object> data) {
        leftNavController.setNavigationTarget(navigationTarget);
    }
}