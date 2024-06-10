package com.bcs05;

import static org.assertj.core.api.Assertions.assertThat;

import com.bcs05.visualization.UI;
import java.awt.*;
import javax.swing.*;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JCheckBoxFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.junit.Test;

public class UITest extends AssertJSwingJUnitTestCase {

    private FrameFixture window;

    @Override
    protected void onSetUp() {
        UI ui = GuiActionRunner.execute(() -> {
            UI uiInstance = new UI();
            uiInstance.setVisible(true); // Make the UI visible for testing
            return uiInstance;
        });

        window = new FrameFixture(robot(), ui);
        window.show(); // Show the frame to test
        robot().waitForIdle(); // Ensure all pending events are processed
    }

    @Test
    public void shouldInitializeComponents() {
        window.requireVisible();
        robot().waitForIdle(); // Ensure all pending events are processed

        // Verify the existence and visibility of the components
        assertThat(window.textBox(textFieldWithLabel("Origin Postal Code:")).target()).isNotNull();
        assertThat(window.textBox(textFieldWithLabel("Destination Postal Code:")).target()).isNotNull();
        assertThat(window.button(JButtonMatcher.withText("Generate")).target()).isNotNull();
        assertThat(window.comboBox(comboBoxWithItems("Bike", "Foot", "Bus", "Aerial")).target()).isNotNull();
        // assertThat(window.checkBox(checkBoxWithText("Aerial
        // Distance")).target()).isNotNull();
        assertThat(window.label(JLabelMatcher.withText("Distance: N/A")).target()).isNotNull();
        assertThat(window.label(JLabelMatcher.withText("Time: N/A")).target()).isNotNull();
    }

    @Test
    public void shouldValidateInput() {
        window.textBox(textFieldWithLabel("Origin Postal Code:")).enterText("6229HD");
        window.textBox(textFieldWithLabel("Destination Postal Code:")).deleteText(); // Clear the field to simulate
                                                                                     // empty input
        window.button(JButtonMatcher.withText("Generate")).click();
        Pause.pause(
                new Condition("Waiting for option pane to appear") {
                    @Override
                    public boolean test() {
                        return window.optionPane().target() != null && window.optionPane().target().isShowing();
                    }
                },
                Timeout.timeout(10000));

        window.optionPane().requireMessage("Invalid postal code(s).").okButton().click();

        Pause.pause(
                new Condition("Waiting for second option pane to appear") {
                    @Override
                    public boolean test() {
                        return window.optionPane().target() != null && window.optionPane().target().isShowing();
                    }
                },
                Timeout.timeout(10000));

        window.optionPane().requireMessage("Please select a mode and enter valid postal codes.").okButton().click();
    }

    @Test
    public void shouldToggleAerialDistance() {

        window.textBox(textFieldWithLabel("Origin Postal Code:")).enterText("6229HD");
        window.textBox(textFieldWithLabel("Destination Postal Code:")).enterText("6222BB");
        window.comboBox(comboBoxWithItems("Bike", "Foot", "Bus", "Aerial")).selectItem("Aerial");

        assertThat(window.comboBox(comboBoxWithItems("Bike", "Foot", "Bus", "Aerial")).selectedItem())
                .isEqualTo("Aerial");
    }

    @Test
    public void shouldSelectMode() {
        window.comboBox(comboBoxWithItems("Bike", "Foot", "Bus", "Aerial")).selectItem("Bike");
        assertThat(window.comboBox(comboBoxWithItems("Bike", "Foot", "Bus", "Aerial")).selectedItem())
                .isEqualTo("Bike");
        window.comboBox(comboBoxWithItems("Bike", "Foot", "Bus", "Aerial")).selectItem("Foot");
        assertThat(window.comboBox(comboBoxWithItems("Bike", "Foot", "Bus", "Aerial")).selectedItem())
                .isEqualTo("Foot");
    }

    @Test
    public void shouldEnableGenerateButtonWithValidInput() {
        window.textBox(textFieldWithLabel("Origin Postal Code:")).enterText("6229HD");
        window.textBox(textFieldWithLabel("Destination Postal Code:")).enterText("6222BB");
        window.comboBox().selectItem("Bike");
        assertThat(window.button(JButtonMatcher.withText("Generate")).isEnabled()).isTrue();
    }

    private GenericTypeMatcher<JTextField> textFieldWithLabel(String labelText) {
        return new GenericTypeMatcher<JTextField>(JTextField.class) {
            @Override
            protected boolean isMatching(JTextField textField) {
                Component parent = textField.getParent();
                if (parent instanceof JPanel) {
                    Component[] components = ((JPanel) parent).getComponents();
                    for (int i = 0; i < components.length; i++) {
                        if (components[i] instanceof JLabel && ((JLabel) components[i]).getText().equals(labelText)) {
                            if (i + 1 < components.length && components[i + 1] == textField) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        };
    }

    private GenericTypeMatcher<JComboBox> comboBoxWithItems(String... items) {
        return new GenericTypeMatcher<JComboBox>(JComboBox.class) {
            @Override
            protected boolean isMatching(JComboBox comboBox) {
                if (comboBox.getItemCount() != items.length) {
                    return false;
                }
                for (int i = 0; i < items.length; i++) {
                    if (!comboBox.getItemAt(i).equals(items[i])) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private GenericTypeMatcher<JCheckBox> checkBoxWithText(String text) {
        return new GenericTypeMatcher<JCheckBox>(JCheckBox.class) {
            @Override
            protected boolean isMatching(JCheckBox checkBox) {
                return checkBox.getText().equals(text);
            }
        };
    }
}
