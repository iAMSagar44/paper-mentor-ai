"use server";
import { EventData } from "./definitions";

export async function aiAssistant(message: string) {
    try {
        const response = await fetch(`${process.env.BACK_END_BASE_URL}/api/chat`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ "question": message }),
        });
        const result: EventData = await response.json();
        //console.log("The AI response is ", result);
        return result;
    } catch (error) {
        console.error("An error occurred while making the API request:", error);
        throw error;
    }
}