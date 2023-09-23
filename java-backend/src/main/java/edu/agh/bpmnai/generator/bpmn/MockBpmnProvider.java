package edu.agh.bpmnai.generator.bpmn;

import edu.agh.bpmnai.generator.TextPrompt;
import edu.agh.bpmnai.generator.bpmn.model.BpmnFile;

public class MockBpmnProvider implements BpmnProvider {
    @Override
    public BpmnFile provideForTextPrompt(TextPrompt prompt) {
        return new BpmnFile("""
                <?xml version="1.0" encoding="UTF-8" standalone="no"?>
                <definitions xmlns:camunda="http://camunda.org/schema/1.0/bpmn" id="definitions_0088a886-7779-459a-b80f-581ff339b554" targetNamespace="http://camunda.org/examples" xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
                  <process id="pizza_delivery_process" name="Pizza Delivery Process">
                    <startEvent id="start_event" name="Order Placed">
                      <outgoing>start_to_record_order</outgoing>
                    </startEvent>
                    <userTask camunda:assignee="" id="record_order_task" name="Record Order Details">
                      <incoming>start_to_record_order</incoming>
                      <outgoing>record_order_to_provide_contact</outgoing>
                    </userTask>
                    <userTask camunda:assignee="" id="provide_contact_task" name="Provide Contact Information">
                      <incoming>record_order_to_provide_contact</incoming>
                      <outgoing>provide_contact_to_review_order</outgoing>
                    </userTask>
                    <serviceTask id="review_order_task" name="Review Order Details">
                      <incoming>provide_contact_to_review_order</incoming>
                      <outgoing>review_order_to_availability_gateway</outgoing>
                    </serviceTask>
                    <exclusiveGateway id="availability_gateway" name="Pizza Availability">
                      <incoming>review_order_to_availability_gateway</incoming>
                      <outgoing>availability_gateway_to_prepare_pizza</outgoing>
                      <outgoing>availability_gateway_to_order_cancelled</outgoing>
                    </exclusiveGateway>
                    <userTask camunda:assignee="" id="prepare_pizza_task" name="Prepare Pizza">
                      <incoming>availability_gateway_to_prepare_pizza</incoming>
                      <outgoing>prepare_pizza_to_notify_driver</outgoing>
                    </userTask>
                    <serviceTask id="notify_driver_task" name="Notify Delivery Driver">
                      <incoming>prepare_pizza_to_notify_driver</incoming>
                      <outgoing>notify_driver_to_package_pizza</outgoing>
                    </serviceTask>
                    <userTask camunda:assignee="" id="package_pizza_task" name="Package Pizza">
                      <incoming>notify_driver_to_package_pizza</incoming>
                      <outgoing>package_pizza_to_deliver_pizza</outgoing>
                    </userTask>
                    <userTask camunda:assignee="" id="deliver_pizza_task" name="Deliver Pizza">
                      <incoming>package_pizza_to_deliver_pizza</incoming>
                      <outgoing>deliver_pizza_to_confirm_delivery</outgoing>
                    </userTask>
                    <userTask camunda:assignee="" id="confirm_delivery_task" name="Confirm Delivery">
                      <incoming>deliver_pizza_to_confirm_delivery</incoming>
                      <outgoing>confirm_delivery_to_payment</outgoing>
                    </userTask>
                    <userTask camunda:assignee="" id="payment_task" name="Process Payment">
                      <incoming>confirm_delivery_to_payment</incoming>
                      <outgoing>payment_to_update_system</outgoing>
                    </userTask>
                    <serviceTask id="update_system_task" name="Update System">
                      <incoming>payment_to_update_system</incoming>
                      <outgoing>update_system_to_review_order</outgoing>
                    </serviceTask>
                    <userTask camunda:assignee="" id="review_order_task_2" name="Review Order">
                      <incoming>update_system_to_review_order</incoming>
                      <outgoing>review_order_to_order_completed</outgoing>
                    </userTask>
                    <endEvent id="order_completed_event" name="Order Completed">
                      <incoming>review_order_to_order_completed</incoming>
                    </endEvent>
                    <endEvent id="order_cancelled_event" name="Order Cancelled">
                      <incoming>availability_gateway_to_order_cancelled</incoming>
                    </endEvent>
                    <sequenceFlow id="start_to_record_order" name="" sourceRef="start_event" targetRef="record_order_task"/>
                    <sequenceFlow id="record_order_to_provide_contact" name="" sourceRef="record_order_task" targetRef="provide_contact_task"/>
                    <sequenceFlow id="provide_contact_to_review_order" name="" sourceRef="provide_contact_task" targetRef="review_order_task"/>
                    <sequenceFlow id="review_order_to_availability_gateway" name="" sourceRef="review_order_task" targetRef="availability_gateway"/>
                    <sequenceFlow id="availability_gateway_to_prepare_pizza" name="" sourceRef="availability_gateway" targetRef="prepare_pizza_task"/>
                    <sequenceFlow id="availability_gateway_to_order_cancelled" name="" sourceRef="availability_gateway" targetRef="order_cancelled_event"/>
                    <sequenceFlow id="prepare_pizza_to_notify_driver" name="" sourceRef="prepare_pizza_task" targetRef="notify_driver_task"/>
                    <sequenceFlow id="notify_driver_to_package_pizza" name="" sourceRef="notify_driver_task" targetRef="package_pizza_task"/>
                    <sequenceFlow id="package_pizza_to_deliver_pizza" name="" sourceRef="package_pizza_task" targetRef="deliver_pizza_task"/>
                    <sequenceFlow id="deliver_pizza_to_confirm_delivery" name="" sourceRef="deliver_pizza_task" targetRef="confirm_delivery_task"/>
                    <sequenceFlow id="confirm_delivery_to_payment" name="" sourceRef="confirm_delivery_task" targetRef="payment_task"/>
                    <sequenceFlow id="payment_to_update_system" name="" sourceRef="payment_task" targetRef="update_system_task"/>
                    <sequenceFlow id="update_system_to_review_order" name="" sourceRef="update_system_task" targetRef="review_order_task_2"/>
                    <sequenceFlow id="review_order_to_order_completed" name="" sourceRef="review_order_task_2" targetRef="order_completed_event"/>
                  </process>
                </definitions>
                """);
    }
}
