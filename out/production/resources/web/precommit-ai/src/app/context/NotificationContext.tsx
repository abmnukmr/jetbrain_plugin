'use client';
import React, { createContext, useState, useContext, ReactNode } from 'react';


type NotificationVariant = 'success' | 'error' | 'warning' | 'info';

type NotificationContextType = {
    showNotification: (
        message: string,
        options?: {
            variant?: NotificationVariant;
            duration?: number;
        }
    ) => void;
};

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

type NotificationData = {
    message: string;
    variant: NotificationVariant;
};

export function NotificationProvider({ children }: { children: ReactNode }) {
    const [notification, setNotification] = useState<NotificationData | null>(null);

    const showNotification: NotificationContextType['showNotification'] = (
        message,
        options = {}
    ) => {
        const variant = options.variant || 'info';
        const duration = options.duration || 3000;

        setNotification({ message, variant });

        setTimeout(() => {
            setNotification(null);
        }, duration);
    };

    const getVariantStyle = (variant: NotificationVariant) => {
        switch (variant) {
            case 'success':
                return 'bg-green-600 text-white';
            case 'error':
                return 'bg-red-600 text-white';
            case 'warning':
                return 'bg-yellow-500 text-black';
            case 'info':
            default:
                return 'bg-blue-600 text-white';
        }
    };

    return (
        <NotificationContext.Provider value={{ showNotification }}>
            {children}
            {notification && (
                <div
                    className={`fixed top-6 left-1/2 transform -translate-x-1/2 px-4 py-2 rounded shadow-lg z-50 transition-opacity duration-300 ${getVariantStyle(notification.variant)}`}
                >
                    {notification.message}
                </div>
            )}
        </NotificationContext.Provider>
    );
}

export const useNotification = () => {
    const context = useContext(NotificationContext);
    if (!context) {
        throw new Error('useNotification must be used within a NotificationProvider');
    }
    return context;
};
