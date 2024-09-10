import Image from "next/image";
import aichat from '../../../../public/ai_human.svg'
import styles from "./chat.module.css";

const ChatLoading = () => {
    return (
        <div className="place-self-start space-y-2">
            <div className="bg-white p-5 rounded-2xl rounded-tl-none">
                <div className="flex items-center">
                    <div>
                        <Image src={aichat} alt="AI" className="w-6 h-6" />
                    </div>
                </div>
                <div className={styles.assistantMessage}>
                    <div className="animate-pulse flex space-x-4">
                        <div className="rounded-full bg-slate-200 h-10 w-10"></div>
                        <div className="flex-1 space-y-6 py-1">
                            <div className="h-2 bg-slate-200 rounded"></div>
                            <div className="space-y-3">
                                <div className="grid grid-cols-3 gap-4">
                                    <div className="h-2 bg-slate-200 rounded col-span-2"></div>
                                    <div className="h-2 bg-slate-200 rounded col-span-1"></div>
                                </div>
                                <div className="h-2 bg-slate-200 rounded"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default ChatLoading;