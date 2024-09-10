import React from "react";
import styles from "./page.module.css";
import AIChat from "../ui/chat-landing/aichat";

const Home = () => {
    return (
        <main className={styles.main}>
            <div className={styles.container}>
                <AIChat />
            </div>
        </main>
    );
};

export default Home;