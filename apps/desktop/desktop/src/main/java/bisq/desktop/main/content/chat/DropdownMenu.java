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

package bisq.desktop.main.content.chat;

import bisq.desktop.common.utils.ImageUtil;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.PopupWindow;
import javafx.stage.WindowEvent;

public class DropdownMenu extends Button {
    private final ContextMenu contextMenu = new ContextMenu();

    public DropdownMenu(String regularIconId, String hoveringIconId) {
        ImageView regularIcon = ImageUtil.getImageViewById(regularIconId);
        ImageView hoveringIcon = ImageUtil.getImageViewById(hoveringIconId);
        setGraphic(regularIcon);

        getStyleClass().add("dropdown-menu");

        double size = 29;
        setMaxSize(size, size);
        setMinSize(size, size);
        setPrefSize(size, size);

        attachHideListeners();
        setOnAction(event -> toggleContextMenu());

        contextMenu.getStyleClass().add("dropdown-menu-popup");
        contextMenu.setOnShowing(e -> {
            getStyleClass().add("dropdown-menu-active");
            setGraphic(hoveringIcon);
        });
        contextMenu.setOnHidden(e -> {
            getStyleClass().remove("dropdown-menu-active");
            setGraphic(regularIcon);
        });
        setOnMouseExited(e -> setGraphic(contextMenu.isShowing() ? hoveringIcon : regularIcon));
        setOnMouseEntered(e -> setGraphic(hoveringIcon));
    }

    private void toggleContextMenu() {
        if (!contextMenu.isShowing()) {
            contextMenu.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_TOP_RIGHT);
            Bounds bounds = this.localToScreen(this.getBoundsInLocal());
            double x = bounds.getMaxX() - 10; // Removing padding
            double y = bounds.getMaxY() - 3;
            contextMenu.show(this, x, y);
        } else {
            contextMenu.hide();
        }
    }

    public void addMenuItems(MenuItem... items) {
        contextMenu.getItems().addAll(items);
    }

    public void clearMenuItems() {
        contextMenu.getItems().clear();
    }

    private void attachHideListeners() {
        this.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.addEventHandler(WindowEvent.WINDOW_HIDING, e -> contextMenu.hide());
                    }
                });
            }
        });
    }
}