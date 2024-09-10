import React from "react";
import styles from "./page.module.css";
import StreamChat from "@/app/ui/chat-landing/chat";

const Home = () => {
    return (
        <main className={styles.main}>
            <div className={styles.container}>
                <StreamChat />
            </div>
        </main>
    );
};

export default Home;