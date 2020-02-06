#include "nodeview.h"
#include "nodescene.h"

#include <QDebug>
#include <QMouseEvent>
#include <QScrollBar>


NodeView::NodeView(NodeScene *scene, QWidget *parent)
{
    Q_UNUSED(parent)

    this->setScene(scene);
    timer = new QTimer(this);

    connect(timer, SIGNAL(timeout()), this, SLOT(autoScroll()));
}

NodeView::NodeView(QWidget *parent)
{
    Q_UNUSED(parent)

    timer = new QTimer(this);

    connect(timer, SIGNAL(timeout()), this, SLOT(autoScroll()));
}

// Destructor likely is incomplete, to be checked later
NodeView::~NodeView()
{
    delete timer;
}

void NodeView::zoomIn()
{
    if (transform().m11() < 2)
    {
        qreal m11 = transform().m11() + 0.1;
        qreal m22 = transform().m22() + 0.1;

        setTransform(QTransform(m11, 0, 0, 0, m22, 0, 0, 0, 1));
    }
}

void NodeView::zoomOut()
{
    if (transform().m11() > 0.7)
    {
        qreal m11 = transform().m11() - 0.1;
        qreal m22 = transform().m22() - 0.1;

        setTransform(QTransform(m11, 0, 0, 0, m22, 0, 0, 0, 1));
    }
}

void NodeView::autoScroll()
{
    QRectF visibleRect = viewport()->geometry();

    if (visibleRect.right() - mousePos.x() < 30)
    {
        horizontalScrollBar()->setValue(horizontalScrollBar()->value() + 5);
    }
    if (mousePos.x() - visibleRect.left() < 30)
    {
        horizontalScrollBar()->setValue(horizontalScrollBar()->value() - 5);
    }

    if (mousePos.y() - visibleRect.top() < 30)
    {
        verticalScrollBar()->setValue(verticalScrollBar()->value() - 5);
    }
    if (visibleRect.bottom() - mousePos.y() < 30)
    {
        verticalScrollBar()->setValue(verticalScrollBar()->value() + 5);
    }
}

void NodeView::mousePressEvent(QMouseEvent *event)
{
    mousePos = event->pos();
    timer->start(10);
    QGraphicsView::mousePressEvent(event);
}

void NodeView::mouseReleaseEvent(QMouseEvent *event)
{
    timer->stop();
    QGraphicsView::mouseReleaseEvent(event);
}

void NodeView::mouseMoveEvent(QMouseEvent *event)
{
    mousePos = event->pos();
    QGraphicsView::mouseMoveEvent(event);
}

void NodeView::keyPressEvent(QKeyEvent *event)
{
    pressedKeys.insert(event->key());

    if (pressedKeys.size() == 2 && pressedKeys.find(Qt::Key_Control) != pressedKeys.end())
    {
        if (pressedKeys.find(Qt::Key_Plus) != pressedKeys.end())
        {
            zoomIn();
        }
        else if (pressedKeys.find(Qt::Key_Minus) != pressedKeys.end())
        {
            zoomOut();
        }
    }

    QGraphicsView::keyPressEvent(event);
}

void NodeView::keyReleaseEvent(QKeyEvent *event)
{
    pressedKeys.erase(event->key());

    QGraphicsView::keyReleaseEvent(event);
}
