#ifndef CONNECTIONSOCKET_H
#define CONNECTIONSOCKET_H

#include <QGraphicsItem>

class Connection;

class ConnectionSocket : public QGraphicsItem
{
public:
    enum { Type = UserType + 1 };

    ConnectionSocket(QGraphicsItem *parent = nullptr,
                     int _direction = 0,
                     QColor _color = Qt::white,
                     QPointF _pos = QPointF(0, 0));

    QRectF boundingRect() const override;
    void paint(QPainter *painter, const QStyleOptionGraphicsItem *option, QWidget *widget) override;

    void addConnection(Connection *connection);
    void removeConnection();
    void nodeMoved();
    void trackSignal(QPointF pos);

    int type() const override;
    int getDirection();

    bool hasConnectionObject();
    bool isConnected();
    bool possibleConnection(ConnectionSocket *socket);

    Connection* getConnection();

    QPointF scenePos();

protected:
    void mousePressEvent(QGraphicsSceneMouseEvent *event) override;
    void mouseReleaseEvent(QGraphicsSceneMouseEvent *event) override;
    void mouseMoveEvent(QGraphicsSceneMouseEvent *event) override;
    void hoverEnterEvent(QGraphicsSceneHoverEvent *event) override;
    void hoverLeaveEvent(QGraphicsSceneHoverEvent *event) override;

    QVariant itemChange(GraphicsItemChange change, const QVariant &value) override;

private:
    QColor color;
    Connection *connection = nullptr;

    int direction; // 1 = input, 0 = output

    bool hovered = false;
    bool pressed = false;
};

#endif // CONNECTIONSOCKET_H
