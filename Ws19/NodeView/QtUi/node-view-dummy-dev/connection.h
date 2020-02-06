#ifndef CONNECTION_H
#define CONNECTION_H

#include <QGraphicsLineItem>

class ConnectionSocket;

class Connection : public QGraphicsLineItem
{
public:
    Connection(ConnectionSocket *fromSocket, ConnectionSocket *toSocket);
    ~Connection() override;

    ConnectionSocket* fromSocket() const;
    ConnectionSocket* toSocket() const;

    void setFromSocket(ConnectionSocket *socket);
    void setToSocket(ConnectionSocket *socket);

    void paint(QPainter *painter, const QStyleOptionGraphicsItem *option, QWidget *widget) override;

    void setColor(QColor color);
    void trackSockets();
    void trackCursor(QPointF pos);

    bool tryConnect(ConnectionSocket *socket);


private:
    ConnectionSocket* _fromSocket = nullptr;
    ConnectionSocket* _toSocket = nullptr;
};

#endif // CONNECTION_H
